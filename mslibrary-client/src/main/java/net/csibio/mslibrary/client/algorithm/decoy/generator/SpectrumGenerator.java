package net.csibio.mslibrary.client.algorithm.decoy.generator;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.algorithm.entropy.Entropy;
import net.csibio.mslibrary.client.constants.Constants;
import net.csibio.mslibrary.client.constants.enums.DecoyStrategy;
import net.csibio.mslibrary.client.domain.bean.spectrum.IonPeak;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import org.apache.commons.math3.stat.StatUtils;
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
            case XYMeta -> xyMeta(spectrumDOS, decoySpectrumDOS, method);
            case SpectralEntropyBased -> spectralEntropyBased(spectrumDOS, decoySpectrumDOS);
            case SpectrumBased -> spectrumBased(spectrumDOS, decoySpectrumDOS, method);
            case IonEntropyBased -> ionEntropyBased(spectrumDOS, decoySpectrumDOS, method);
            case IonEntropyBased2 -> ionEntropyBased2(spectrumDOS, decoySpectrumDOS, method);
            default -> log.error("Decoy procedure {} is currently not supported", method.getDecoyStrategy());
        }

        //insert decoy spectra into database
        decoySpectrumDOS.parallelStream().forEach(spectrumDO -> spectrumDO.setLibraryId(libraryId + SymbolConst.DELIMITER + method.getDecoyStrategy()));
        spectrumService.insert(decoySpectrumDOS, libraryId + SymbolConst.DELIMITER + method.getDecoyStrategy());
        log.info("Decoy spectra generation finished, cost {} ms", System.currentTimeMillis() - start);
    }

    private void spectralEntropyBased(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS) {
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            SpectrumDO decoySpectrumDO = new SpectrumDO();
            decoySpectrumDO.setRawSpectrumId(spectrumDO.getId());
            decoySpectrumDO.setMzs(spectrumDO.getMzs());
            decoySpectrumDO.setInts(spectrumDO.getInts());
            decoySpectrumDO.setPrecursorMz(spectrumDO.getPrecursorMz());
            decoySpectrumDO.setDecoy(true);
            decoySpectrumDOS.add(decoySpectrumDO);
        });
        entropyControl(spectrumDOS, decoySpectrumDOS);
    }

    /**
     * generate decoy spectra by
     * 1. add precursor ion peak
     * 2. randomly add ion peaks from other spectra
     * 3. stop until the number of peaks in decoy is the same as the original spectrum
     */
    private void naive(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS) {
        //Generate decoy spectra
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            //add precursor ion peak
            TreeSet<IonPeak> decoyIonPeaks = new TreeSet<>();
            double diff = Double.MAX_VALUE;
            int precursorIndex = -1;
            for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                if (Math.abs(spectrumDO.getMzs()[i] - spectrumDO.getPrecursorMz()) < diff) {
                    diff = Math.abs(spectrumDO.getMzs()[i] - spectrumDO.getPrecursorMz());
                    precursorIndex = i;
                }
            }
            decoyIonPeaks.add(new IonPeak(spectrumDO.getMzs()[precursorIndex], spectrumDO.getInts()[precursorIndex]));

            //randomly add ion peaks from other spectra
            while (decoyIonPeaks.size() < spectrumDO.getMzs().length) {
                int randomIndex = new Random().nextInt(spectrumDOS.size());
                SpectrumDO randomSpectrumDO = spectrumDOS.get(randomIndex);
                int randomIonIndex = new Random().nextInt(randomSpectrumDO.getMzs().length);
                IonPeak ionPeak = new IonPeak(randomSpectrumDO.getMzs()[randomIonIndex], randomSpectrumDO.getInts()[randomIonIndex]);
                decoyIonPeaks.add(ionPeak);
            }
            decoySpectrumDOS.add(convertIonPeaksToDecoy(new ArrayList<>(decoyIonPeaks), spectrumDO));
        });
    }

    private void ionEntropyBased(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS, MethodDO methodDO) {
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            Double mzTolerance = methodDO.getPpmForMzTolerance() ? methodDO.getPpm() * Constants.PPM * spectrumDO.getPrecursorMz() : methodDO.getMzTolerance();
            List<SpectrumDO> spectraWarehouse = spectrumDOS.stream().filter(librarySpectrumDO -> Math.abs(librarySpectrumDO.getPrecursorMz() - spectrumDO.getPrecursorMz()) < mzTolerance).toList();

            List<IonPeak> ionWarehouse = new ArrayList<>();
            for (SpectrumDO spectrum : spectraWarehouse) {
                int precursorIndex = ArrayUtil.findNearestIndex(spectrum.getMzs(), spectrumDO.getPrecursorMz());
                double precursorIntensity = spectrum.getInts()[precursorIndex];
                for (int i = 0; i < spectrum.getMzs().length; i++) {
                    IonPeak ionPeak = new IonPeak(spectrum.getMzs()[i], spectrum.getInts()[i] / precursorIntensity);
                    ionWarehouse.add(ionPeak);
                }
            }

            HashMap<IonPeak, List<IonPeak>> ionPeakMap = new HashMap<>();
            for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                IonPeak ionPeak = new IonPeak(spectrumDO.getMzs()[i], spectrumDO.getInts()[i]);
                List<IonPeak> ionPeaks = ionWarehouse.stream().filter(ion -> Math.abs(ion.getMz() - ionPeak.getMz()) < mzTolerance).toList();
                ionPeakMap.put(ionPeak, ionPeaks);
            }

            //calculate ion entropy
            for (IonPeak indexIonPeak : ionPeakMap.keySet()) {
                List<IonPeak> ionPeaks = ionPeakMap.get(indexIonPeak);
                double[] ionIntensities = new double[ionPeaks.size()];
                for (int i = 0; i < ionPeaks.size(); i++) {
                    ionIntensities[i] = ionPeaks.get(i).getIntensity();
                }
                double ionEntropy = Entropy.getEntropy(ionIntensities);
                indexIonPeak.setIonEntropy(ionEntropy);
            }

            //sort ion peaks by entropy
            List<IonPeak> ionPeaks = new ArrayList<>(ionPeakMap.keySet());
            ionPeaks.sort(Comparator.comparingDouble(IonPeak::getIonEntropy));

            //process the ion peaks with entropy 0
            List<IonPeak> decoyIonPeaks = new ArrayList<>();
            List<IonPeak> entropyZeroIonPeaks = ionPeaks.stream().filter(ionPeak -> ionPeak.getIonEntropy() == 0).toList();
            List<Double> intensities = new ArrayList<>(entropyZeroIonPeaks.stream().map(IonPeak::getIntensity).toList());
            for (IonPeak ionPeak : entropyZeroIonPeaks) {
                int random = new Random().nextInt(intensities.size());
                decoyIonPeaks.add(new IonPeak(ionPeak.getMz(), intensities.get(random)));
                intensities.remove(random);
            }

            //generate decoy spectrum by reverse the ion intensity by entropy
            ionPeaks.removeIf(ionPeak -> ionPeak.getIonEntropy() == 0);
            for (int i = 0; i < ionPeaks.size(); i++) {
                IonPeak ionPeak = ionPeaks.get(i);
                decoyIonPeaks.add(new IonPeak(ionPeak.getMz(), ionPeaks.get(ionPeaks.size() - i - 1).getIntensity()));
            }
            decoySpectrumDOS.add(convertIonPeaksToDecoy(decoyIonPeaks, spectrumDO));
        });
    }

    private void ionEntropyBased2(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS, MethodDO methodDO) {
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            Double mzTolerance = methodDO.getPpmForMzTolerance() ? methodDO.getPpm() * Constants.PPM * spectrumDO.getPrecursorMz() : methodDO.getMzTolerance();
            List<SpectrumDO> spectraWarehouse = spectrumDOS.stream().filter(librarySpectrumDO -> Math.abs(librarySpectrumDO.getPrecursorMz() - spectrumDO.getPrecursorMz()) < mzTolerance).toList();

            List<IonPeak> ionWarehouse = new ArrayList<>();
            for (SpectrumDO spectrum : spectraWarehouse) {
                double baseIntensity = StatUtils.max(spectrum.getInts());
                for (int i = 0; i < spectrum.getMzs().length; i++) {
                    IonPeak ionPeak = new IonPeak(spectrum.getMzs()[i], spectrum.getInts()[i] / baseIntensity * 100);
                    ionWarehouse.add(ionPeak);
                }
            }

            HashMap<IonPeak, List<IonPeak>> ionPeakMap = new HashMap<>();
            for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                IonPeak ionPeak = new IonPeak(spectrumDO.getMzs()[i], spectrumDO.getInts()[i]);
                List<IonPeak> ionPeaks = ionWarehouse.stream().filter(ion -> Math.abs(ion.getMz() - ionPeak.getMz()) < mzTolerance).toList();
                ionPeakMap.put(ionPeak, ionPeaks);
            }

            //calculate ion entropy
            for (IonPeak indexIonPeak : ionPeakMap.keySet()) {
                List<IonPeak> ionPeaks = ionPeakMap.get(indexIonPeak);
                double[] ionIntensities = new double[ionPeaks.size()];
                for (int i = 0; i < ionPeaks.size(); i++) {
                    ionIntensities[i] = ionPeaks.get(i).getIntensity();
                }
                double ionEntropy = Entropy.getEntropy(ionIntensities);
                indexIonPeak.setIonEntropy(ionEntropy);
            }

            //sort ion peaks by entropy
            List<IonPeak> ionPeaks = new ArrayList<>(ionPeakMap.keySet());
            ionPeaks.sort(Comparator.comparingDouble(IonPeak::getIonEntropy));

            //process the ion peaks with entropy 0
            List<IonPeak> decoyIonPeaks = new ArrayList<>();
            List<IonPeak> entropyZeroIonPeaks = ionPeaks.stream().filter(ionPeak -> ionPeak.getIonEntropy() == 0).toList();
            List<Double> intensities = new ArrayList<>(entropyZeroIonPeaks.stream().map(IonPeak::getIntensity).toList());
            for (IonPeak ionPeak : entropyZeroIonPeaks) {
                int random = new Random().nextInt(intensities.size());
                decoyIonPeaks.add(new IonPeak(ionPeak.getMz(), intensities.get(random)));
                intensities.remove(random);
            }

            //generate decoy spectrum by reverse the ion intensity by entropy
            ionPeaks.removeIf(ionPeak -> ionPeak.getIonEntropy() == 0);
            for (int i = 0; i < ionPeaks.size(); i++) {
                IonPeak ionPeak = ionPeaks.get(i);
                decoyIonPeaks.add(new IonPeak(ionPeak.getMz(), ionPeaks.get(ionPeaks.size() - i - 1).getIntensity()));
            }
            decoySpectrumDOS.add(convertIonPeaksToDecoy(decoyIonPeaks, spectrumDO));
        });
    }

    /**
     * 1. For each target spectrum, build a signal warehouse S
     * 2. S contains all ions which are smaller than the precursorMz from spectra with more than one
     * 3. remove a certain proportion of the ions in the target spectrum
     * 4. randomly select ions from S to fill the decoy spectrum making sure that it has the same ions as the target spectrum
     * 5. finally, 30% of the ions in the decoy spectrum is randomly selected to shift +/- precursorMz/200,000
     */
    private void xyMeta(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS, MethodDO methodDO) {
        double removeProportion = 0.5;
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            //1. find spectra contains the precursorMz
            Double mzTolerance = methodDO.getPpmForMzTolerance() ? methodDO.getPpm() * Constants.PPM * spectrumDO.getPrecursorMz() : methodDO.getMzTolerance();
            List<SpectrumDO> spectraWarehouse = spectrumDOS.stream().filter(spectrum -> ArrayUtil.findNearestDiff(spectrum.getMzs(), spectrumDO.getPrecursorMz()) < mzTolerance).toList();

            //2. get all ions which are smaller than precursorMz
            TreeSet<IonPeak> ionWarehouse = new TreeSet<>();
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
                while (ionWarehouse.size() < 5 * removeNum) {
                    int random = new Random().nextInt(spectrumDOS.size());
                    SpectrumDO randomSpectrum = spectrumDOS.get(random);
                    int randomIndex = new Random().nextInt(randomSpectrum.getMzs().length);
                    IonPeak randomIonPeak = new IonPeak(randomSpectrum.getMzs()[randomIndex], randomSpectrum.getInts()[randomIndex]);
                    ionWarehouse.add(randomIonPeak);
                }
            }

            //4. randomly select ions from S to fill the decoy spectrum
            List<IonPeak> decoyIonPeaks = new ArrayList<>();
            List<IonPeak> ionWarehouseList = new ArrayList<>(ionWarehouse);
            for (int i = 0; i < removeNum; i++) {
                int random = new Random().nextInt(ionWarehouseList.size());
                IonPeak randomIonPeak = ionWarehouseList.get(random);
                decoyIonPeaks.add(randomIonPeak);
                ionWarehouseList.remove(random);
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
            decoySpectrumDOS.add(convertIonPeaksToDecoy(decoyIonPeaks, spectrumDO));
        });
    }

    /**
     * generate decoy spectra by spectrumBased method
     * 1. add precursor ion peak to decoy spectrum
     * 2. find set of peaks of all spectra containing the added peak
     * 3. randomly select a peak from the set and add it to the decoy spectrum
     * 4. repeat step 3 until the number of peaks in the decoy spectrum is the same as the number of peaks in the target spectrum
     */
    private void spectrumBased(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS, MethodDO methodDO) {
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            //1. add precursor ion peak to decoy spectrum
            TreeSet<IonPeak> decoyIonPeaks = new TreeSet<>();
            int precursorIndex = ArrayUtil.findNearestIndex(spectrumDO.getMzs(), spectrumDO.getPrecursorMz());
            IonPeak precursorIonPeak = new IonPeak(spectrumDO.getMzs()[precursorIndex], spectrumDO.getInts()[precursorIndex]);
            decoyIonPeaks.add(precursorIonPeak);
            double lastAddedMz = spectrumDO.getPrecursorMz();

            //repeat the following steps until the number of peaks in the decoy spectrum mimics target spectrum
            while (decoyIonPeaks.size() < spectrumDO.getMzs().length) {
                //2. find set of peaks of all spectra containing the added peak
                Double mzTolerance = methodDO.getPpmForMzTolerance() ? methodDO.getPpm() * Constants.PPM * lastAddedMz : methodDO.getMzTolerance();
                double finalLastAddedMz = lastAddedMz;
                List<SpectrumDO> candidateSpectra = spectrumDOS.stream().filter(spectrum -> ArrayUtil.findNearestDiff(spectrum.getMzs(), finalLastAddedMz) < mzTolerance).toList();
                Double tempMzTolerance = mzTolerance;
                while (candidateSpectra.size() == 0) {
                    tempMzTolerance += tempMzTolerance * 2;
                    double finalTempMzTolerance = tempMzTolerance;
                    candidateSpectra = spectrumDOS.stream().filter(spectrum -> ArrayUtil.findNearestDiff(spectrum.getMzs(), finalLastAddedMz) < finalTempMzTolerance).toList();
                }

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
                int randomIndex = new Random().nextInt(candidateIonPeaks.size());
                IonPeak randomIonPeak = candidateIonPeaks.get(randomIndex);
                decoyIonPeaks.add(randomIonPeak);
                lastAddedMz = randomIonPeak.getMz();
            }
            if (decoyIonPeaks.size() == spectrumDO.getMzs().length) {
                decoySpectrumDOS.add(convertIonPeaksToDecoy(new ArrayList<>(decoyIonPeaks), spectrumDO));
            }
        });
    }

    /**
     * Convert ionPeaks to SpectrumDO
     */
    private SpectrumDO convertIonPeaksToDecoy(List<IonPeak> ionPeaks, SpectrumDO rawSpectrumDO) {
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
        spectrumDO.setPrecursorMz(rawSpectrumDO.getPrecursorMz());
        spectrumDO.setDecoy(true);
        spectrumDO.setRawSpectrumId(rawSpectrumDO.getId());
        return spectrumDO;
    }

    private void entropyControl(List<SpectrumDO> spectrumDOS, List<SpectrumDO> decoySpectrumDOS) {
        HashMap<String, SpectrumDO> spectrumMap = new HashMap<>();
        spectrumDOS.forEach(spectrumDO -> spectrumMap.put(spectrumDO.getId(), spectrumDO));
        decoySpectrumDOS.parallelStream().forEach(decoySpectrum -> {
            SpectrumDO rawSpectrumDO = spectrumMap.get(decoySpectrum.getRawSpectrumId());
            List<Double> rawSpectrumInts = new ArrayList<>(Arrays.stream(rawSpectrumDO.getInts()).boxed().toList());
            double[] decoySpectrumInts = new double[decoySpectrum.getInts().length];
            //randomly set intensity in decoy spectrum same to raw spectrum
            for (int i = 0; i < decoySpectrumInts.length; i++) {
                int randomIndex = new Random().nextInt(rawSpectrumInts.size());
                decoySpectrumInts[i] = rawSpectrumInts.get(randomIndex);
                rawSpectrumInts.remove(randomIndex);
            }
            decoySpectrum.setInts(decoySpectrumInts);
        });
    }

}
