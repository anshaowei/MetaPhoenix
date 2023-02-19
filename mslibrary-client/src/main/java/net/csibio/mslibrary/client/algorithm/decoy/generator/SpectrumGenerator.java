package net.csibio.mslibrary.client.algorithm.decoy.generator;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.constants.Constants;
import net.csibio.mslibrary.client.constants.enums.DecoyStrategy;
import net.csibio.mslibrary.client.domain.bean.spectrum.IonPeak;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("spectrumGenerator")
@Slf4j
public class SpectrumGenerator {

    @Autowired
    SpectrumService spectrumService;

    public void execute(String libraryId, MethodDO method) {
        long start = System.currentTimeMillis();
        log.info("Start to generate decoy spectra on library: {} by {} method", libraryId, method.getDecoyStrategy());

        //initialize
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> decoySpectrumDOS = Collections.synchronizedList(new ArrayList<>());
        DecoyStrategy strategy = DecoyStrategy.valueOf(method.getDecoyStrategy());

        //process with different strategies
        switch (strategy) {
            case Naive -> naive(spectrumDOS, decoySpectrumDOS);
            case XYMeta -> xymeta(spectrumDOS, decoySpectrumDOS, method);
            case SpectrumBased -> spectrumBased(spectrumDOS, decoySpectrumDOS, method);
            case FragmentationTree -> fragmentationTree(spectrumDOS, decoySpectrumDOS);
            case Entropy_1 -> entropy_1(spectrumDOS, decoySpectrumDOS, method);
            case Entropy_2 -> entropy_2(spectrumDOS, decoySpectrumDOS, method);
            default -> log.error("Decoy procedure {} is not supported", method.getDecoyStrategy());
        }

        //insert decoy spectra into database
        decoySpectrumDOS.parallelStream().forEach(spectrumDO -> spectrumDO.setLibraryId(libraryId + SymbolConst.DELIMITER + method.getDecoyStrategy()));
        spectrumService.insert(decoySpectrumDOS, libraryId + SymbolConst.DELIMITER + method.getDecoyStrategy());
        log.info("Decoy spectra generation finished, cost {} ms", System.currentTimeMillis() - start);
    }

    /**
     * generate decoy spectra by
     * 1. add precursor ion peak
     * 2. randomly add ion peaks from other spectra
     * 3. stop until the number of peaks in decoy is the same as the original spectrum
     */
    private void naive(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS) {
        List<IonPeak> ionPeaksWithoutPrecursor = new ArrayList<>();
        HashMap<String, IonPeak> precursorIonPeakMap = new HashMap<>();

        //Collect all ions except precursor ion
        for (SpectrumDO spectrumDO : spectrumDOS) {
            ionPeaksWithoutPrecursor.addAll(separatePrecursorIonPeak(spectrumDO, precursorIonPeakMap));
        }

        //Generate decoy spectra
        for (SpectrumDO spectrumDO : spectrumDOS) {
            //insert precursor ion peak
            List<IonPeak> decoyIonPeaks = new ArrayList<>();
            IonPeak precursorIonPeak = precursorIonPeakMap.get(spectrumDO.getId());
            decoyIonPeaks.add(precursorIonPeak);

            //randomly add ion peaks from other spectra
            for (int i = 0; i < spectrumDO.getMzs().length - 1; i++) {
                int randomIndex = new Random().nextInt(ionPeaksWithoutPrecursor.size());
                IonPeak ionPeak = ionPeaksWithoutPrecursor.get(randomIndex);
                decoyIonPeaks.add(ionPeak);
                ionPeaksWithoutPrecursor.remove(randomIndex);
            }
            decoySpectrumDOS.add(convertIonPeaksToSpectrum(decoyIonPeaks, spectrumDO.getPrecursorMz()));
        }
    }

    private void entropy_1(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS, MethodDO method) {
        List<IonPeak> ionPeaksWithoutPrecursor = new ArrayList<>();
        HashMap<String, IonPeak> precursorIonPeakMap = new HashMap<>();
        HashMap<String, List<IonPeak>> ionPeakMap = new HashMap<>();
        for (SpectrumDO spectrumDO : spectrumDOS) {
            List<IonPeak> otherIonPeaks = separatePrecursorIonPeak(spectrumDO, precursorIonPeakMap);
            ionPeaksWithoutPrecursor.addAll(otherIonPeaks);
            ionPeakMap.put(spectrumDO.getId(), otherIonPeaks);
        }
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            //1. insert precursor ion peak
            TreeSet<IonPeak> decoyIonPeaks = new TreeSet<>();
            IonPeak precursorIonPeak = precursorIonPeakMap.get(spectrumDO.getId());
            decoyIonPeaks.add(precursorIonPeak);

            //2. randomly select an ion from other spectra
            while (decoyIonPeaks.size() < spectrumDO.getMzs().length) {
                int randomIndex = new Random().nextInt(ionPeaksWithoutPrecursor.size());
                IonPeak ionPeak = ionPeaksWithoutPrecursor.get(randomIndex);
                decoyIonPeaks.add(ionPeak);
            }

            //3. arrange the ion intensity from the original spectrum
            List<IonPeak> ionPeaks = new ArrayList<>();
            CollectionUtils.addAll(ionPeaks, new Object[ionPeakMap.get(spectrumDO.getId()).size()]);
            Collections.copy(ionPeaks, ionPeakMap.get(spectrumDO.getId()));
            ionPeaks.add(precursorIonPeak);
            for (IonPeak decoyIonPeak : decoyIonPeaks) {
                int randomIndex = new Random().nextInt(ionPeaks.size());
                IonPeak ionPeak = ionPeaks.get(randomIndex);
                decoyIonPeak.setIntensity(ionPeak.getIntensity());
                ionPeaks.remove(randomIndex);
            }

            decoySpectrumDOS.add(convertIonPeaksToSpectrum(new ArrayList<>(decoyIonPeaks), spectrumDO.getPrecursorMz()));
        });
    }

    private void entropy_2(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS, MethodDO method) {
        List<IonPeak> ionPeaksWithoutPrecursor = new ArrayList<>();
        HashMap<String, IonPeak> precursorIonPeakMap = new HashMap<>();
        HashMap<String, List<IonPeak>> ionPeakMap = new HashMap<>();
        for (SpectrumDO spectrumDO : spectrumDOS) {
            List<IonPeak> otherIonPeaks = separatePrecursorIonPeak(spectrumDO, precursorIonPeakMap);
            ionPeaksWithoutPrecursor.addAll(otherIonPeaks);
            ionPeakMap.put(spectrumDO.getId(), otherIonPeaks);
        }
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            //1. insert precursor ion peak
            TreeSet<IonPeak> decoyIonPeaks = new TreeSet<>();
            IonPeak precursorIonPeak = precursorIonPeakMap.get(spectrumDO.getId());
            decoyIonPeaks.add(precursorIonPeak);

            //2. find spectra contains the precursor ion mz
            List<SpectrumDO> spectraWarehouse = spectrumDOS.stream().filter(spectrum -> ArrayUtil.findNearestDiff(spectrum.getMzs(), spectrumDO.getPrecursorMz()) < method.getMzTolerance()).toList();

            //3. get all ions which are smaller than precursorMz
            List<IonPeak> ionWarehouse = new ArrayList<>();
            spectraWarehouse.forEach(spectrum -> {
                for (int i = 0; i < spectrum.getMzs().length; i++) {
                    if (spectrum.getMzs()[i] < spectrumDO.getPrecursorMz()) {
                        IonPeak ionPeak = new IonPeak(spectrum.getMzs()[i], spectrum.getInts()[i]);
                        ionWarehouse.add(ionPeak);
                    }
                }
            });

            //4. if ionWarehouse is too small, enlarge it with random ions
            if (ionWarehouse.size() < spectrumDO.getMzs().length) {
                for (int i = 0; i < spectrumDO.getMzs().length * 5; i++) {
                    SpectrumDO randomSpectrum = spectrumDOS.get(new Random().nextInt(spectrumDOS.size()));
                    int randomIndex = new Random().nextInt(randomSpectrum.getMzs().length);
                    IonPeak randomIonPek = new IonPeak(randomSpectrum.getMzs()[randomIndex], randomSpectrum.getInts()[randomIndex]);
                    ionWarehouse.add(randomIonPek);
                }
            }

            //5. randomly get mzs from the ionWarehouse
            while (decoyIonPeaks.size() < spectrumDO.getMzs().length) {
                int randomIndex = new Random().nextInt(ionWarehouse.size());
                decoyIonPeaks.add(ionWarehouse.get(randomIndex));
            }

            //6. arrange the ion intensity from the original spectrum
            List<IonPeak> ionPeaks = new ArrayList<>();
            CollectionUtils.addAll(ionPeaks, new Object[ionPeakMap.get(spectrumDO.getId()).size()]);
            Collections.copy(ionPeaks, ionPeakMap.get(spectrumDO.getId()));
            ionPeaks.add(precursorIonPeak);
            for (IonPeak decoyIonPeak : decoyIonPeaks) {
                int randomIndex = new Random().nextInt(ionPeaks.size());
                IonPeak ionPeak = ionPeaks.get(randomIndex);
                decoyIonPeak.setIntensity(ionPeak.getIntensity());
                ionPeaks.remove(randomIndex);
            }
            decoySpectrumDOS.add(convertIonPeaksToSpectrum(new ArrayList<>(decoyIonPeaks), spectrumDO.getPrecursorMz()));
        });
    }

    /**
     * optimized naive method
     */
    private void optNaive(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS, MethodDO method) {
        List<IonPeak> ionPeaksWithoutPrecursor = new ArrayList<>();
        HashMap<String, IonPeak> precursorIonPeakMap = new HashMap<>();
        HashMap<String, List<IonPeak>> ionPeakMap = new HashMap<>();
        for (SpectrumDO spectrumDO : spectrumDOS) {
            List<IonPeak> otherIonPeaks = separatePrecursorIonPeak(spectrumDO, precursorIonPeakMap);
            ionPeaksWithoutPrecursor.addAll(otherIonPeaks);
            ionPeakMap.put(spectrumDO.getId(), otherIonPeaks);
        }
        for (SpectrumDO spectrumDO : spectrumDOS) {
            //1. insert precursor ion peak
            List<IonPeak> decoyIonPeaks = new ArrayList<>();
            List<IonPeak> ionPeaks = ionPeakMap.get(spectrumDO.getId());
            IonPeak precursorIonPeak = precursorIonPeakMap.get(spectrumDO.getId());
            decoyIonPeaks.add(precursorIonPeak);

            //2. 从剩余谱图的所有ionPeak中随机挑选若干，使得target和decoy谱图的ionPeak数量相同，且decoy的信号值为随机的打乱后的原谱图信号值（保证熵相同）
            for (int i = 0; i < spectrumDO.getMzs().length - 1; i++) {
                //对每张谱图，不要小于precursorMz的或者与已加入的谱图重复的
                int randomIndex = new Random().nextInt(ionPeaksWithoutPrecursor.size());
                IonPeak ionPeak = ionPeaksWithoutPrecursor.get(randomIndex);
                if (ionPeak.getMz() >= precursorIonPeak.getMz()) {
                    i--;
                    continue;
                }
                boolean repeat = false;
                for (IonPeak decoyIonPeak : decoyIonPeaks) {
                    if (Math.abs(ionPeak.getMz() - decoyIonPeak.getMz()) < (method.getPpmForMzTolerance() ? method.getPpm() * Constants.PPM * ionPeak.getMz() : method.getMzTolerance())) {
                        i--;
                        repeat = true;
                        break;
                    }
                }
                if (repeat)
                    continue;

                //到此处的时候ionPeak的mz的随机选取已经完成，下一步进行ionPeak的强度改变
                int random = new Random().nextInt(ionPeaks.size());
                IonPeak randomIonPeak = ionPeaks.get(random);
                ionPeak.setIntensity(randomIonPeak.getIntensity());
                ionPeaks.remove(random);
                decoyIonPeaks.add(ionPeak);
            }
            decoySpectrumDOS.add(convertIonPeaksToSpectrum(decoyIonPeaks, spectrumDO.getPrecursorMz()));
        }
    }

    /**
     * 1. For each target spectrum, build a signal warehouse S
     * 2. S contains all ions which are smaller than the precursorMz from spectra with more than one
     * 3. remove a certain proportion of the ions in the target spectrum
     * 4. randomly select ions from S to fill the decoy spectrum making sure that it has the same ions as the target spectrum
     * 5. finally, 30% of the ions in the decoy spectrum is randomly selected to shift +/- precursorMz/200,000
     */
    private void xymeta(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS, MethodDO methodDO) {
        double removeProportion = 0.5;
        spectrumDOS.parallelStream().forEach(spectrumDO -> {

            //1. find spectra contains the precursorMz
            Double mzTolerance = methodDO.getPpmForMzTolerance() ? methodDO.getPpm() * Constants.PPM * spectrumDO.getPrecursorMz() : methodDO.getMzTolerance();
            List<SpectrumDO> spectraWarehouse = spectrumDOS.stream().filter(spectrum -> ArrayUtil.findNearestDiff(spectrum.getMzs(), spectrumDO.getPrecursorMz()) < mzTolerance).toList();

            //2. get all ions which are smaller than precursorMz
            List<IonPeak> ionWarehouse = new ArrayList<>();
            spectraWarehouse.forEach(spectrum -> {
                for (int i = 0; i < spectrum.getMzs().length; i++) {
                    if (spectrum.getMzs()[i] < spectrumDO.getPrecursorMz()) {
                        IonPeak ionPeak = new IonPeak(spectrum.getMzs()[i], spectrum.getInts()[i]);
                        ionWarehouse.add(ionPeak);
                    }
                }
            });

            //3. remove a certain proportion of the ions in the target spectrum
            List<IonPeak> targetIonPeaks = new ArrayList<>();
            for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                IonPeak ionPeak = new IonPeak(spectrumDO.getMzs()[i], spectrumDO.getInts()[i]);
                targetIonPeaks.add(ionPeak);
            }
            int removeNum = (int) (targetIonPeaks.size() * removeProportion);
            for (int i = 0; i < removeNum; i++) {
                targetIonPeaks.remove(new Random().nextInt(targetIonPeaks.size()));
            }

            //3.5 fill S if S is not enough, fill it with random ions
            if (ionWarehouse.size() <= removeNum) {
                for (int i = 0; i < 2 * removeNum; i++) {
                    int random = new Random().nextInt(spectrumDOS.size());
                    SpectrumDO randomSpectrum = spectrumDOS.get(random);
                    int randomIndex = new Random().nextInt(randomSpectrum.getMzs().length);
                    IonPeak randomIonPeak = new IonPeak(randomSpectrum.getMzs()[randomIndex], randomSpectrum.getInts()[randomIndex]);
                    ionWarehouse.add(randomIonPeak);
                }
            }

            //4. randomly select ions from S to fill the decoy spectrum
            List<IonPeak> decoyIonPeaks = new ArrayList<>();
            for (int i = 0; i < removeNum; i++) {
                int random = new Random().nextInt(ionWarehouse.size());
                IonPeak randomIonPeak = ionWarehouse.get(random);
                decoyIonPeaks.add(randomIonPeak);
                ionWarehouse.remove(random);
            }
            decoyIonPeaks.addAll(targetIonPeaks);

            //5. 30% of the ions in the decoy spectrum is randomly selected to shift +/- precursorMz/200,000
            int shiftNum = (int) (decoyIonPeaks.size() * 0.3);
            for (int i = 0; i < shiftNum; i++) {
                int random = new Random().nextInt(decoyIonPeaks.size());
                IonPeak randomIonPeak = decoyIonPeaks.get(random);
                double shift = new Random().nextDouble() * spectrumDO.getPrecursorMz() / 200000;
                if (new Random().nextBoolean()) {
                    randomIonPeak.setMz(randomIonPeak.getMz() + shift);
                } else {
                    randomIonPeak.setMz(randomIonPeak.getMz() - shift);
                }
            }
            decoySpectrumDOS.add(convertIonPeaksToSpectrum(decoyIonPeaks, spectrumDO.getPrecursorMz()));
        });
    }

    public void fragmentationTree(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS) {
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            SpectrumDO decoySpectrumDO = new SpectrumDO();
            decoySpectrumDO.setMzs(spectrumDO.getMzs());
            decoySpectrumDO.setInts(spectrumDO.getInts());
            decoySpectrumDO.setPrecursorMz(spectrumDO.getPrecursorMz());
            decoySpectrumDOS.add(decoySpectrumDO);
        });
    }

    /**
     * generate decoy spectra by spectrumBased method
     * 1. add precursor ion peak to decoy spectrum
     * 2. find set of peaks of all spectra containing the added peak
     * 3. randomly select a peak from the set and add it to the decoy spectrum
     * 4. repeat step 3 until the number of peaks in the decoy spectrum is the same as the number of peaks in the target spectrum
     */
    public void spectrumBased(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS, MethodDO methodDO) {
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            //1. add precursor ion peak to decoy spectrum
            List<IonPeak> decoyIonPeaks = new ArrayList<>();
            int precursorIndex = ArrayUtil.findNearestIndex(spectrumDO.getMzs(), spectrumDO.getPrecursorMz());
            IonPeak precursorIonPeak = new IonPeak(spectrumDO.getMzs()[precursorIndex], spectrumDO.getInts()[precursorIndex]);
            decoyIonPeaks.add(precursorIonPeak);
            double lastAddedMz = spectrumDO.getPrecursorMz();

            //repeat the following steps until the number of peaks in the decoy spectrum mimics target spectrum
            for (int i = 0; i < spectrumDO.getMzs().length - 1; i++) {
                //2. find set of peaks of all spectra containing the added peak
                Double mzTolerance = methodDO.getPpmForMzTolerance() ? methodDO.getPpm() * Constants.PPM * lastAddedMz : methodDO.getMzTolerance();
                double finalLastAddedMz = lastAddedMz;
                List<SpectrumDO> candidateSpectra = spectrumDOS.stream().filter(spectrum -> ArrayUtil.findNearestDiff(spectrum.getMzs(), finalLastAddedMz) < mzTolerance).toList();

                //3. draw 5 ions from each spectrum and add them to the candidate ion peak set
                List<IonPeak> candidateIonPeaks = new ArrayList<>();
                for (SpectrumDO candidateSpectrum : candidateSpectra) {
                    int limitCount = 5;
                    if (candidateSpectrum.getMzs().length <= limitCount) {
                        for (int j = 0; j < candidateSpectrum.getMzs().length; j++) {
                            IonPeak ionPeak = new IonPeak(candidateSpectrum.getMzs()[j], candidateSpectrum.getInts()[j]);
                            candidateIonPeaks.add(ionPeak);
                        }
                    } else {
                        for (int j = 0; j < limitCount; j++) {
                            int randomIndex = new Random().nextInt(candidateSpectrum.getMzs().length);
                            IonPeak ionPeak = new IonPeak(candidateSpectrum.getMzs()[randomIndex], candidateSpectrum.getInts()[randomIndex]);
                            candidateIonPeaks.add(ionPeak);
                        }
                    }
                }

                //4. randomly select a peak from the set and add it to the decoy spectrum
                if (candidateIonPeaks.size() == 0) {
                    continue;
                }
                int randomIndex = new Random().nextInt(candidateIonPeaks.size());
                IonPeak randomIonPeak = candidateIonPeaks.get(randomIndex);

                //5. if the ion mz is the same as the last added mz, skip this ion
                if (randomIonPeak.getMz() == lastAddedMz) {
                    i--;
                    continue;
                }

                //6. record the last added peak
                decoyIonPeaks.add(randomIonPeak);
                lastAddedMz = randomIonPeak.getMz();
            }
            decoySpectrumDOS.add(convertIonPeaksToSpectrum(decoyIonPeaks, spectrumDO.getPrecursorMz()));
        });
    }

    /**
     * separate precursor ion peak from ion peaks
     */
    private List<IonPeak> separatePrecursorIonPeak(SpectrumDO spectrumDO, HashMap<String, IonPeak> precursorIonPeakMap) {
        List<IonPeak> ionPeaks = new ArrayList<>();
        double diff = Double.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < spectrumDO.getMzs().length; i++) {
            if (Math.abs(spectrumDO.getMzs()[i] - spectrumDO.getPrecursorMz()) < diff) {
                diff = Math.abs(spectrumDO.getMzs()[i] - spectrumDO.getPrecursorMz());
                index = i;
            }
            IonPeak ionPeak = new IonPeak(spectrumDO.getMzs()[i], spectrumDO.getInts()[i]);
            ionPeaks.add(ionPeak);
        }
        ionPeaks.remove(index);
        precursorIonPeakMap.put(spectrumDO.getId(), new IonPeak(spectrumDO.getMzs()[index], spectrumDO.getInts()[index]));
        return ionPeaks;
    }

    /**
     * Convert ionPeaks to SpectrumDO
     */
    private SpectrumDO convertIonPeaksToSpectrum(List<IonPeak> ionPeaks, double precursorMz) {
        ionPeaks.sort(Comparator.comparing(IonPeak::getMz));
        SpectrumDO spectrumDO = new SpectrumDO();
        double[] mzs = new double[ionPeaks.size()];
        double[] intensities = new double[ionPeaks.size()];
        for (int i = 0; i < ionPeaks.size(); i++) {
            mzs[i] = ionPeaks.get(i).getMz();
            intensities[i] = ionPeaks.get(i).getIntensity();
        }
        spectrumDO.setMzs(mzs);
        spectrumDO.setInts(intensities);
        spectrumDO.setPrecursorMz(precursorMz);
        return spectrumDO;
    }

}
