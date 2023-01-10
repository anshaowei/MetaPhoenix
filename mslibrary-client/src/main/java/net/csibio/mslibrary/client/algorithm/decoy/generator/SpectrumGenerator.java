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
        log.info("开始执行naive方法生成伪肽段");
        long start = System.currentTimeMillis();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);

        List<SpectrumDO> decoySpectrumDOS = new ArrayList<>();
        List<IonPeak> allIonPeaks = new ArrayList<>();
        for (SpectrumDO spectrumDO : spectrumDOS) {
            allIonPeaks.addAll(getIonPeaksWithoutPrecursor(spectrumDO));
        }

        //针对每一张谱图生成decoy谱图
        for (SpectrumDO spectrumDO : spectrumDOS) {
            //提取谱图中precursorMz的peak
            int precursorIndex = ArrayUtil.findNearestIndex(spectrumDO.getMzs(), spectrumDO.getPrecursorMz());
            IonPeak precursorIonPeak = new IonPeak(spectrumDO.getMzs()[precursorIndex], spectrumDO.getInts()[precursorIndex]);

            //从剩余谱图的所有ionPeak中随机挑选若干，使得target和decoy谱图的ionPeak数量相同，且谱图熵相同
            List<IonPeak> decoyIonPeaks = new ArrayList<>();
            for (int i = 0; i < spectrumDO.getMzs().length - 1; i++) {
                int randomIndex = new Random().nextInt(allIonPeaks.size());
                decoyIonPeaks.add(allIonPeaks.get(randomIndex));
                allIonPeaks.remove(randomIndex);
            }

            //混合precursorIonPeak和decoyIonPeaks并生成谱图
            List<IonPeak> mixedIonPeaks = new ArrayList<>();
            mixedIonPeaks.add(precursorIonPeak);
            mixedIonPeaks.addAll(decoyIonPeaks);
            mixedIonPeaks.sort((o1, o2) -> {
                if (o1.getMz() > o2.getMz()) {
                    return 1;
                } else if (o1.getMz() < o2.getMz()) {
                    return -1;
                } else {
                    return 0;
                }
            });
            double[] mzs = new double[mixedIonPeaks.size()];
            double[] intensities = new double[mixedIonPeaks.size()];
            for (int i = 0; i < mixedIonPeaks.size(); i++) {
                mzs[i] = mixedIonPeaks.get(i).getMz();
                intensities[i] = mixedIonPeaks.get(i).getIntensity();
            }
            SpectrumDO decoySpectrumDO = new SpectrumDO();
            decoySpectrumDO.setMzs(mzs);
            decoySpectrumDO.setInts(intensities);
            decoySpectrumDO.setPrecursorMz(spectrumDO.getPrecursorMz());
            decoySpectrumDOS.add(decoySpectrumDO);
        }
        long end = System.currentTimeMillis();
        log.info("naive方法生成伪肽段完成，耗时{}ms", end - start);
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

            //2. 迭代性地向空的伪谱图中迭代加入若干个信号点
            for (int i = 0; i < spectrumDO.getMzs().length - 1; i++) {
                //2.1 找到包含上一个添加的mz的所有谱图
                List<SpectrumDO> candidateSpectra = findCandidateSpectra(spectrumDOS, lastAddedMz);

                //2.2 生成候选ionPeak集合，每张谱图选择5个信号，谱图不足5个信号则全选
                List<IonPeak> candidateIonPeaks = new ArrayList<>();
                for (SpectrumDO candidateSpectrum : candidateSpectra) {
                    candidateIonPeaks.addAll(getLimitedIonPeaks(candidateSpectrum, 5));
                }

                //2.3 从候选ionPeak集合中排除上一个已经添加的mz
                double finalLastAddedMz = lastAddedMz;
                candidateIonPeaks.removeIf(ionPeak -> Math.abs(ionPeak.getMz() - finalLastAddedMz) < 5 * Constants.PPM * finalLastAddedMz);

                //2.4 从候选ionPeak集合中随机选择一个加入到伪谱图中
                int randomIndex = new Random().nextInt(candidateIonPeaks.size());
                IonPeak randomIonPeak = candidateIonPeaks.get(randomIndex);
                decoyIonPeaks.add(randomIonPeak);

                //2.5 重新设置最后添加的mz
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
        }

        int a = 9;

    }

    public void fragmentationTree(String libraryId) {

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

    private List<IonPeak> getIonPeaksWithoutPrecursor(SpectrumDO spectrumDO) {
        List<IonPeak> ionPeaks = new ArrayList<>();
        int index = ArrayUtil.findNearestIndex(spectrumDO.getMzs(), spectrumDO.getPrecursorMz());
        for (int i = 0; i < spectrumDO.getMzs().length; i++) {
            if (i == index) {
                continue;
            }
            IonPeak ionPeak = new IonPeak(spectrumDO.getMzs()[i], spectrumDO.getInts()[i]);
            ionPeaks.add(ionPeak);
        }
        return ionPeaks;
    }

    /**
     * 提取一个谱图中的ionPeak
     *
     * @param spectrumDO
     * @return
     */
    private List<IonPeak> getIonPeaks(SpectrumDO spectrumDO) {
        List<IonPeak> ionPeaks = new ArrayList<>();
        for (int i = 0; i < spectrumDO.getMzs().length; i++) {
            IonPeak ionPeak = new IonPeak(spectrumDO.getMzs()[i], spectrumDO.getInts()[i]);
            ionPeaks.add(ionPeak);
        }
        return ionPeaks;
    }

    /**
     * 根据限制数量提取一个谱图中的ionPeak
     *
     * @param spectrumDO
     * @param limitCount
     * @return
     */
    private List<IonPeak> getLimitedIonPeaks(SpectrumDO spectrumDO, int limitCount) {
        if (limitCount > spectrumDO.getMzs().length) {
            return getIonPeaks(spectrumDO);
        } else {
            List<IonPeak> ionPeaks = new ArrayList<>();
            //generate limitCount random numbers less than spectrumDO.getMzs().length
            List<Integer> randomIndexes = new ArrayList<>();
            for (int i = 0; i < limitCount; i++) {
                int randomIndex = (int) (Math.random() * spectrumDO.getMzs().length);
                while (randomIndexes.contains(randomIndex)) {
                    randomIndex = (int) (Math.random() * spectrumDO.getMzs().length);
                }
                randomIndexes.add(randomIndex);
            }
            for (int i = 0; i < limitCount; i++) {
                IonPeak ionPeak = new IonPeak(spectrumDO.getMzs()[randomIndexes.get(i)], spectrumDO.getInts()[randomIndexes.get(i)]);
                ionPeaks.add(ionPeak);
            }
            return ionPeaks;
        }
    }

}
