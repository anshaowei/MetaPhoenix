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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Component("spectrumGenerator")
@Slf4j
public class SpectrumGenerator {

    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;

    public void naive(String libraryId) {
        log.info("开始执行naive方法生成伪谱图");
        long start = System.currentTimeMillis();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> decoySpectrumDOS = new ArrayList<>();
        List<IonPeak> ionPeaksWithoutPrecursor = new ArrayList<>();
        HashMap<String, IonPeak> precursorIonPeakMap = new HashMap<>();
        for (SpectrumDO spectrumDO : spectrumDOS) {
            ionPeaksWithoutPrecursor.addAll(separatePrecursorIonPeak(spectrumDO, precursorIonPeakMap));
        }

        //针对每一张谱图生成decoy谱图
        for (SpectrumDO spectrumDO : spectrumDOS) {
            //1. 将precursor的peak插入到谱图中
            List<IonPeak> decoyIonPeaks = new ArrayList<>();
            IonPeak precursorIonPeak = precursorIonPeakMap.get(spectrumDO.getId());
            decoyIonPeaks.add(precursorIonPeak);

            //2. 从剩余谱图的所有ionPeak中随机挑选若干，使得target和decoy谱图的ionPeak数量相同
            for (int i = 0; i < spectrumDO.getMzs().length - 1; i++) {
                int randomIndex = new Random().nextInt(ionPeaksWithoutPrecursor.size());
                decoyIonPeaks.add(ionPeaksWithoutPrecursor.get(randomIndex));
                ionPeaksWithoutPrecursor.remove(randomIndex);
            }

            //3. 将decoyIonPeaks生成谱图
            decoyIonPeaks.sort((o1, o2) -> {
                if (o1.getMz() > o2.getMz()) {
                    return 1;
                } else if (o1.getMz() < o2.getMz()) {
                    return -1;
                } else {
                    return 0;
                }
            });
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
        log.info("naive方法生成伪肽段完成，耗时{}ms", end - start);
        spectrumService.insert(decoySpectrumDOS, libraryId + "-decoy");
    }

    public void entropyBased(String libraryId) {
        log.info("开始执行entropy方法生成伪谱图");
        long start = System.currentTimeMillis();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> decoySpectrumDOS = new ArrayList<>();
        List<IonPeak> ionPeaksWithoutPrecursor = new ArrayList<>();
        HashMap<String, IonPeak> precursorIonPeakMap = new HashMap<>();
        for (SpectrumDO spectrumDO : spectrumDOS) {
            ionPeaksWithoutPrecursor.addAll(separatePrecursorIonPeak(spectrumDO, precursorIonPeakMap));
        }

        //针对每一张谱图生成decoy谱图
        for (SpectrumDO spectrumDO : spectrumDOS) {
            //1. 将precursor的peak插入到谱图中
            List<IonPeak> decoyIonPeaks = new ArrayList<>();
            IonPeak precursorIonPeak = precursorIonPeakMap.get(spectrumDO.getId());
            decoyIonPeaks.add(precursorIonPeak);

            //2. 从剩余谱图的所有ionPeak中随机挑选若干，使得target和decoy谱图的ionPeak数量相同
            for (int i = 0; i < spectrumDO.getMzs().length - 1; i++) {
                int randomIndex = new Random().nextInt(ionPeaksWithoutPrecursor.size());
                IonPeak ionPeak = ionPeaksWithoutPrecursor.get(randomIndex);
                if (decoyIonPeaks.contains(ionPeak)) {
                    i--;
                    continue;
                }
                decoyIonPeaks.add(ionPeaksWithoutPrecursor.get(randomIndex));
            }

            //3. 将decoyIonPeaks生成谱图
            decoyIonPeaks.sort((o1, o2) -> {
                if (o1.getMz() > o2.getMz()) {
                    return 1;
                } else if (o1.getMz() < o2.getMz()) {
                    return -1;
                } else {
                    return 0;
                }
            });
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
        log.info("entropy方法生成伪肽段完成，耗时{}ms", end - start);
        spectrumService.insert(decoySpectrumDOS, libraryId + "-decoy");
    }

    public void spectrumBased(String libraryId) {

        log.info("开始执行SpectrumBased方法生成伪肽段");
        long start = System.currentTimeMillis();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> decoySpectrumDOS = new ArrayList<>();

        for (SpectrumDO spectrumDO : spectrumDOS) {
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
            decoyIonPeaks.sort((o1, o2) -> {
                if (o1.getMz() > o2.getMz()) {
                    return 1;
                } else if (o1.getMz() < o2.getMz()) {
                    return -1;
                } else {
                    return 0;
                }
            });
            double[] mzs = new double[decoyIonPeaks.size()];
            double[] intensities = new double[decoyIonPeaks.size()];
            for (int i = 0; i < decoyIonPeaks.size(); i++) {
                mzs[i] = decoyIonPeaks.get(i).getMz();
                intensities[i] = decoyIonPeaks.get(i).getIntensity();
            }
            decoySpectrum.setMzs(mzs);
            decoySpectrum.setInts(intensities);
            decoySpectrum.setPrecursorMz(spectrumDO.getPrecursorMz());
            decoySpectrumDOS.add(decoySpectrum);
        }

        log.info("SpectrumBased方法生成伪肽段完成，耗时{}ms", System.currentTimeMillis() - start);
        spectrumService.insert(decoySpectrumDOS, libraryId + "-decoy");
        log.info("伪肽段库{}已经生成", libraryId + "-decoy");

    }

    /**
     * 找到一组谱图中包含给定mz的谱图
     *
     * @param spectrumDOS
     * @param mz
     * @return
     */
    private List<SpectrumDO> findCandidateSpectra(List<SpectrumDO> spectrumDOS, double mz) {
        List<SpectrumDO> candidates = new ArrayList<>();
        for (SpectrumDO spectrumDO : spectrumDOS) {
            if (ArrayUtil.findNearestDiff(spectrumDO.getMzs(), mz) < 5 * Constants.PPM * mz) {
                candidates.add(spectrumDO);
            }
        }
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

}
