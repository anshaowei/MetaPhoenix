package net.csibio.mslibrary.client.algorithm.search;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.bean.common.Spectrum;
import net.csibio.aird.enums.MsLevel;
import net.csibio.mslibrary.client.algorithm.score.SpectrumScorer;
import net.csibio.mslibrary.client.domain.bean.identification.Feature;
import net.csibio.mslibrary.client.domain.bean.identification.IdentificationForm;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.bean.params.IdentificationParams;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class MetaProSearch {

    @Autowired
    LibraryService libraryService;
    @Autowired
    CompoundService compoundService;
    @Autowired
    SpectrumService spectrumService;
    @Autowired
    SpectrumScorer spectrumScorer;

    public IdentificationForm identifyFeatures(IdentificationForm identificationForm, IdentificationParams identificationParams) {

        List<Feature> features = identificationForm.getFeatures();
        //针对每个化合物库进行检索

        features.parallelStream().forEach(feature -> {
            //策略1：严格匹配满足仪器设备平台配置的谱图
            List<SpectrumDO> spectrumDOS = new ArrayList<>();
            for (String libraryId : identificationParams.getLibraryIds()) {
                SpectrumQuery spectrumQuery = new SpectrumQuery();
                spectrumQuery.setPrecursorMz(feature.getMz());
                spectrumQuery.setMzTolerance(identificationParams.getMzTolerance());
                spectrumQuery.setLibraryId(libraryId);
                spectrumQuery.setMsLevel(MsLevel.MS2.getCode());
                if (identificationParams.getStrategy().equals(1)) {
                    //TODO 仪器平台配置暂时不支持能量匹配
                    spectrumQuery.setInstrument(identificationForm.getInstrument());
                    spectrumQuery.setIonSource(identificationForm.getIonSource());
                }
                spectrumDOS.addAll(spectrumService.getAll(spectrumQuery, libraryId));
            }
            spectrumDOS = spectrumDOS.parallelStream().filter(spectrumDO -> Math.abs(spectrumDO.getPrecursorMz() - feature.getMz()) <= identificationParams.getMzTolerance()).toList();

            //对MS2谱图的匹配程度进行打分
            List<LibraryHit> libraryHits = new ArrayList<>();
            for (SpectrumDO spectrumDO : spectrumDOS) {
                Double similarityScore = 0.0;
                Spectrum ms2Spectrum = feature.getMs2Spectrum();
                Spectrum libSpectrum = spectrumDO.getSpectrum();
                double ms2ForwardScore = spectrumScorer.ms2ForwardScore(ms2Spectrum, libSpectrum, identificationParams.getMzTolerance());
                double ms2ReverseScore = spectrumScorer.ms2ReverseScore(libSpectrum, ms2Spectrum, identificationParams.getMzTolerance());
                similarityScore += ms2ForwardScore + ms2ReverseScore;

                //命中谱图结果填充
                LibraryHit libraryHit = new LibraryHit();
                libraryHit.setSpectrumId(spectrumDO.getSpectrumId());
                libraryHit.setCompoundName(spectrumDO.getCompoundName());
                libraryHit.setLibraryName(spectrumDO.getLibraryId());
                libraryHit.setAdduct(spectrumDO.getAdduct());
                libraryHit.setPrecursorMz(spectrumDO.getPrecursorMz());
                libraryHit.setSmiles(spectrumDO.getSmiles());
                libraryHit.setInChI(spectrumDO.getInchI());
                libraryHit.setUrl(spectrumDO.getUrl());
                libraryHit.setMatchScore(similarityScore);
                libraryHits.add(libraryHit);
            }

            //取打分排名前若干名的谱图
            libraryHits.sort(Comparator.comparing(LibraryHit::getMatchScore));
            if (libraryHits.size() >= identificationParams.getTopN()) {
                libraryHits = libraryHits.subList(libraryHits.size() - identificationParams.getTopN(), libraryHits.size());
            }
            feature.setLibraryHits(libraryHits);
        });
        return identificationForm;
    }

}
