package net.csibio.mslibrary.client.algorithm.search;

import lombok.extern.slf4j.Slf4j;
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
        for (String libraryId : identificationParams.getLibraryIds()) {
            features.parallelStream().forEach(feature -> {

                //策略1：严格匹配满足仪器设备平台配置的谱图
                if (identificationParams.getStrategy().equals(1)) {
                    SpectrumQuery spectrumQuery = new SpectrumQuery();
                    spectrumQuery.setLibraryId(libraryId);
                    spectrumQuery.setInstrument(identificationForm.getInstrument());
                    spectrumQuery.setPrecursorMz(feature.getMz());
                    spectrumQuery.setIonSource(identificationForm.getIonSource());

                    List<SpectrumDO> spectrumDOS = spectrumService.getAll(spectrumQuery, libraryId);

                    List<LibraryHit> libraryHits = new ArrayList<>();
                    feature.setLibraryHits(libraryHits);
                }

                //策略2：主要以谱图匹配分数为主，其他信息作为辅助参考信息
                if (identificationParams.getStrategy().equals(2)) {
                    SpectrumQuery spectrumQuery = new SpectrumQuery();
                    spectrumQuery.setLibraryId(libraryId);
                    spectrumQuery.setInstrument(identificationForm.getInstrument());
                    spectrumQuery.setPrecursorMz(feature.getMz());
                    spectrumQuery.setIonSource(identificationForm.getIonSource());

                    List<SpectrumDO> spectrumDOS = spectrumService.getAll(spectrumQuery, libraryId);

                    List<LibraryHit> libraryHits = new ArrayList<>();
                    feature.setLibraryHits(libraryHits);
                }
            });
        }


//        for (String libraryId : identificationParams.getLibraryIds()) {
//            List<SpectrumDO> spectrumDOS = spectrumService.getByPrecursorMz(feature.getMz(), libraryId);
//            if (spectrumDOS.size() == 0) {
//                continue;
//            }
//            Map<Integer, List<SpectrumDO>> spectrumMap = spectrumDOS.stream().collect(Collectors.groupingBy(SpectrumDO::getMsLevel));
//            List<SpectrumDO> ms2LibSpectra = spectrumMap.get(2);
//            if (ms2LibSpectra == null || ms2LibSpectra.size() == 0) {
//                continue;
//            }
//            for (SpectrumDO spectrumDO : ms2LibSpectra) {
//                //PrecursorMz打分
//                Double precursorMzScore = Math.abs(spectrumDO.getPrecursorMz() - feature.getMz()) / feature.getMz();
//
//                //谱图相似性打分
//                Double similarityScore = 0.0;
//                if (feature.getMs2Spectrum() != null) {
//                    Spectrum ms2Spectrum = feature.getMs2Spectrum();
//                    Spectrum libSpectrum = spectrumDO.getSpectrum();
//                    double ms2ForwardScore = spectrumScorer.ms2ForwardScore(ms2Spectrum, libSpectrum, identificationParams.getMzTolerance());
//                    double ms2ReverseScore = spectrumScorer.ms2ReverseScore(libSpectrum, ms2Spectrum, identificationParams.getMzTolerance());
//                    similarityScore += ms2ForwardScore + ms2ReverseScore;
//                }
//
//                //计算总分，两个分数权重一样
//                double matchScore = precursorMzScore + similarityScore;
//
//                //鉴定结果填充
//                LibraryHit libraryHit = new LibraryHit();
//                libraryHit.setCompoundId(spectrumDO.getCompoundId());
//                libraryHit.setSpectrumId(spectrumDO.getSpectrumId());
//                libraryHit.setCompoundName(spectrumDO.getCompoundName());
//                libraryHit.setLibraryName(spectrumDO.getLibraryMembership());
//                libraryHit.setAdduct(spectrumDO.getAdduct());
//                libraryHit.setPrecursorMz(spectrumDO.getPrecursorMz());
//                libraryHit.setSmiles(spectrumDO.getSmiles());
//                libraryHit.setInChI(spectrumDO.getInchI());
//                libraryHit.setUrl(spectrumDO.getUrl());
//                libraryHit.setMatchScore(matchScore);
//                libraryHits.add(libraryHit);
//            }
//        }
//        libraryHits.sort(Comparator.comparing(LibraryHit::getMatchScore));
//
//        //取分数最大的前几个
//        if (libraryHits.size() >= identificationParams.getTopN()) {
//            libraryHits = libraryHits.subList(libraryHits.size() - identificationParams.getTopN(), libraryHits.size());
//        }
//        feature.setLibraryHits(libraryHits);

        return identificationForm;
    }

}
