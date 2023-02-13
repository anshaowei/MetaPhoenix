package net.csibio.mslibrary.client.algorithm.decoy.generator;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.Constants;
import net.csibio.mslibrary.client.domain.bean.spectrum.IonPeak;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("spectrumGenerator")
@Slf4j
public class SpectrumGenerator {

    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;

    /**
     * generate decoy spectra by
     * 1. add precursor ion peak
     * 2. randomly add ion peaks from other spectra
     * 3. stop until the number of peaks in decoy is the same as the original spectrum
     *
     * @param libraryId
     */
    public void naive(String libraryId) {
        log.info("Start to generate decoy spectra by naive method on library: {}", libraryId);
        long start = System.currentTimeMillis();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> decoySpectrumDOS = new ArrayList<>();
        List<IonPeak> ionPeaksWithoutPrecursor = new ArrayList<>();
        HashMap<String, IonPeak> precursorIonPeakMap = new HashMap<>();
        for (SpectrumDO spectrumDO : spectrumDOS) {
            ionPeaksWithoutPrecursor.addAll(separatePrecursorIonPeak(spectrumDO, precursorIonPeakMap));
        }

        for (SpectrumDO spectrumDO : spectrumDOS) {
            //1. insert precursor ion peak
            List<IonPeak> decoyIonPeaks = new ArrayList<>();
            IonPeak precursorIonPeak = precursorIonPeakMap.get(spectrumDO.getId());
            decoyIonPeaks.add(precursorIonPeak);

            //2. randomly add ion peaks from other spectra
            for (int i = 0; i < spectrumDO.getMzs().length - 1; i++) {
                int randomIndex = new Random().nextInt(ionPeaksWithoutPrecursor.size());
                IonPeak ionPeak = ionPeaksWithoutPrecursor.get(randomIndex);
                decoyIonPeaks.add(ionPeak);
                ionPeaksWithoutPrecursor.remove(randomIndex);
            }

            //3. convert to SpectrumDO
            decoySpectrumDOS.add(convertIonPeaksToSpectrum(decoyIonPeaks, spectrumDO.getPrecursorMz()));
        }
        long end = System.currentTimeMillis();
        log.info("Finished generating decoy spectra on {} by naive method, cost {}ms", libraryId, end - start);
        spectrumService.insert(decoySpectrumDOS, libraryId + "-naive");
    }

    public void optNaive(String libraryId) {
        log.info("Start to generate decoy spectra by optNaive method on library: {}", libraryId);
        double mzTolerance = 0.01;
        long start = System.currentTimeMillis();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> decoySpectrumDOS = new ArrayList<>();
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
                    if (Math.abs(ionPeak.getMz() - decoyIonPeak.getMz()) < mzTolerance) {
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

            //3. 将decoyIonPeaks生成谱图
            decoyIonPeaks.sort(Comparator.comparing(IonPeak::getMz));
            double[] mzs = new double[decoyIonPeaks.size()];
            double[] intensities = new double[decoyIonPeaks.size()];
            for (int i = 0; i < decoyIonPeaks.size(); i++) {
                mzs[i] = decoyIonPeaks.get(i).getMz();
                intensities[i] = decoyIonPeaks.get(i).getIntensity();
            }
            SpectrumDO decoySpectrumDO = new SpectrumDO();
            decoySpectrumDO.setMzs(mzs);
            decoySpectrumDO.setInts(intensities);
            decoySpectrumDO.setPrecursorMz(spectrumDO.getPrecursorMz());
            decoySpectrumDOS.add(decoySpectrumDO);
        }
        long end = System.currentTimeMillis();
        log.info("Finished generating decoy spectra on {} by optNaive method, cost {}ms", libraryId, end - start);
        spectrumService.insert(decoySpectrumDOS, libraryId + "-optNaive");
    }

    /**
     * 1. For each target spectrum, build a signal warehouse S
     * 2. S contains all ions which are smaller than the precursorMz from spectra with more than one
     * 3. remove a certain proportion of the ions in the target spectrum
     * 4. randomly select ions from S to fill the decoy spectrum making sure that it has the same ions as the target spectrum
     * 5. finally, 30% of the ions in the decoy spectrum is randomly selected to shift +/- precursorMz/200,000
     *
     * @param libraryId
     */
    public void XYMeta(String libraryId) {
        Double mzTolerance = 0.01;
        double removeProportion = 0.5;
        log.info("Start to generate decoy spectra by XYMeta method on library: {}", libraryId);
        long start = System.currentTimeMillis();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> decoySpectrumDOS = Collections.synchronizedList(new ArrayList<>());

        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            //1. find spectra contains the precursorMz
            List<SpectrumDO> candidateSpectra = findCandidateSpectra(spectrumDOS, spectrumDO.getPrecursorMz());
            //2. get all ions which are smaller than precursorMz
            List<IonPeak> ionWarehouse = new ArrayList<>();
            candidateSpectra.forEach(spectrum -> {
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
            //6. convert to spectrumDO
            decoySpectrumDOS.add(convertIonPeaksToSpectrum(decoyIonPeaks, spectrumDO.getPrecursorMz()));
        });
        long end = System.currentTimeMillis();
        spectrumService.insert(decoySpectrumDOS, libraryId + "-XYMeta");
        log.info("Finished generating decoy spectra on {} by XYMeta method, cost {}ms", libraryId, end - start);
    }

    public void fragmentationTree(String libraryId) {
        log.info("Start to generate decoy spectra by fragmentationTree method on library: {}", libraryId);
        long start = System.currentTimeMillis();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> decoySpectrumDOS = Collections.synchronizedList(new ArrayList<>());
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            SpectrumDO decoySpectrumDO = new SpectrumDO();
            decoySpectrumDO.setMzs(spectrumDO.getMzs());
            decoySpectrumDO.setInts(spectrumDO.getInts());
            decoySpectrumDO.setPrecursorMz(spectrumDO.getPrecursorMz());
            decoySpectrumDOS.add(decoySpectrumDO);
        });
        long end = System.currentTimeMillis();
        spectrumService.insert(decoySpectrumDOS, libraryId + "-fragmentationTree");
        log.info("Finished generating decoy spectra on {} by fragmentationTree method, cost {}ms", libraryId, end - start);
    }

    /**
     * generate decoy spectra by spectrumBased method
     * 1. add precursor ion peak to decoy spectrum
     * 2. find set of peaks of all spectra containing the added peak
     * 3. randomly select a peak from the set and add it to the decoy spectrum
     * 4. repeat step 3 until the number of peaks in the decoy spectrum is the same as the number of peaks in the target spectrum
     *
     * @param libraryId
     */
    public void spectrumBased(String libraryId) {
        log.info("Start to generate decoy spectra by spectrumBased method on library: {}", libraryId);
        long start = System.currentTimeMillis();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> decoySpectrumDOS = Collections.synchronizedList(new ArrayList<>());

        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            SpectrumDO decoySpectrum = new SpectrumDO();
            List<IonPeak> decoyIonPeaks = new ArrayList<>();

            //1. 将precursor所代表的ionPeak加入到伪谱图中
            int precursorIndex = ArrayUtil.findNearestIndex(spectrumDO.getMzs(), spectrumDO.getPrecursorMz());
            IonPeak precursorIonPeak = new IonPeak(spectrumDO.getMzs()[precursorIndex], spectrumDO.getInts()[precursorIndex]);
            decoyIonPeaks.add(precursorIonPeak);
            double lastAddedMz = spectrumDO.getPrecursorMz();

            //2. 迭代性地向空的伪谱图中迭代加入若干个信号点，此步骤有可能会导致伪谱图中的信号点数量小于target谱图
            for (int i = 0; i < spectrumDO.getMzs().length - 1; i++) {
                //2.1 找到包含上一个添加的mz的所有谱图
                List<SpectrumDO> candidateSpectra = findCandidateSpectra(spectrumDOS, lastAddedMz);

                //2.2 生成候选ionPeak集合，每张谱图选择5个信号，谱图不足5个信号则全选
                List<IonPeak> candidateIonPeaks = new ArrayList<>();
                for (SpectrumDO candidateSpectrum : candidateSpectra) {
                    candidateIonPeaks.addAll(getLimitedIonPeaks(candidateSpectrum, 5, spectrumDO.getPrecursorMz(), lastAddedMz));
                }

                //2.3 从候选ionPeak集合中随机选择一个加入到伪谱图中
                if (candidateIonPeaks.size() == 0) {
                    continue;
                }
                int randomIndex = new Random().nextInt(candidateIonPeaks.size());
                IonPeak randomIonPeak = candidateIonPeaks.get(randomIndex);
                decoyIonPeaks.add(randomIonPeak);

                //2.4 重新设置最后添加的mz
                lastAddedMz = randomIonPeak.getMz();
            }

            //3. 将伪谱图转换为SpectrumDO
            decoySpectrumDOS.add(convertIonPeaksToSpectrum(decoyIonPeaks, spectrumDO.getPrecursorMz()));
        });
        long end = System.currentTimeMillis();
        log.info("Finished generating decoy spectra on {} by spectrumBased method, cost {}ms", libraryId, end - start);
        spectrumService.insert(decoySpectrumDOS, libraryId + "-spectrumBased");
    }

    /**
     * find spectra containing the given mz
     *
     * @param spectrumDOS
     * @param mz
     * @return
     */
    private List<SpectrumDO> findCandidateSpectra(List<SpectrumDO> spectrumDOS, double mz) {
        List<SpectrumDO> candidates = Collections.synchronizedList(new ArrayList<>());
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            if (ArrayUtil.findNearestDiff(spectrumDO.getMzs(), mz) < 10 * Constants.PPM * mz) {
                candidates.add(spectrumDO);
            }
        });
        return candidates;
    }

    /**
     * 根据限制数量提取一个谱图中的ionPeak，不选择大于precursorMz或已经添加过mz的ionPeak
     *
     * @param spectrumDO
     * @param limitCount
     * @return
     */
    private List<IonPeak> getLimitedIonPeaks(SpectrumDO spectrumDO, int limitCount, double precursorMz, double lastAddedMz) {

        //找到所有小于precursorMz的ionPeak
        List<IonPeak> ionPeaks = new ArrayList<>();
        for (int i = 0; i < spectrumDO.getMzs().length; i++) {
            if (spectrumDO.getMzs()[i] > precursorMz) {
                break;
            }
            IonPeak ionPeak = new IonPeak(spectrumDO.getMzs()[i], spectrumDO.getInts()[i]);
            ionPeaks.add(ionPeak);
        }

        //排除已经添加过的mz
        ionPeaks.removeIf(ionPeak -> Math.abs(ionPeak.getMz() - lastAddedMz) < 5 * Constants.PPM * lastAddedMz);

        //随机选择limitCount个ionPeak
        if (limitCount >= ionPeaks.size()) {
            return ionPeaks;
        } else {
            List<IonPeak> randomIonPeaks = new ArrayList<>();
            List<Integer> randomIndexes = new ArrayList<>();
            for (int i = 0; i < limitCount; i++) {
                int randomIndex = (int) (Math.random() * ionPeaks.size());
                while (randomIndexes.contains(randomIndex)) {
                    randomIndex = (int) (Math.random() * ionPeaks.size());
                }
                randomIndexes.add(randomIndex);
            }
            for (int randomIndex : randomIndexes) {
                randomIonPeaks.add(ionPeaks.get(randomIndex));
            }
            return randomIonPeaks;
        }
    }

    /**
     * Get ions from the spectrum and divide them into precursor ion and fragment ions
     *
     * @param spectrumDO
     * @param precursorIonPeakMap
     * @return
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
     *
     * @param ionPeaks
     * @param precursorMz
     * @return
     */
    private SpectrumDO convertIonPeaksToSpectrum(List<IonPeak> ionPeaks, double precursorMz) {
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
