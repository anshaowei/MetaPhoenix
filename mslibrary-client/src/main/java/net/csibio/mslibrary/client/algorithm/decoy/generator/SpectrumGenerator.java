package net.csibio.mslibrary.client.algorithm.decoy.generator;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.bean.spectrum.IonPeak;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.client.utils.SpectrumUtil;
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
            allIonPeaks.addAll(spectrumDO.getIonPeaksWithoutPrecursor());
        }

        //针对每一张谱图生成decoy谱图
        for (SpectrumDO spectrumDO : spectrumDOS) {
            //提取谱图中precursorMz的peak
            int precursorIndex = SpectrumUtil.findNearestIndex(spectrumDO.getMzs(), spectrumDO.getPrecursorMz());
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

    }

    public void fragmentationTree(String libraryId) {

    }

}
