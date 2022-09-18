package net.csibio.mslibrary.client.algorithm.search;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.algorithm.score.SpectrumScorer;
import net.csibio.mslibrary.client.domain.bean.identification.Feature;
import net.csibio.mslibrary.client.domain.bean.identification.IdentificationInfo;
import net.csibio.mslibrary.client.domain.bean.params.IdentificationParams;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Identification {

    @Autowired
    LibraryService libraryService;
    @Autowired
    CompoundService compoundService;
    @Autowired
    SpectrumService spectrumService;
    @Autowired
    SpectrumScorer spectrumScorer;

    /**
     * 根据MS2谱图进行匹配，并将打分最高的几个谱图的结果填充返回
     *
     * @param feature
     * @param identificationParams
     * @return
     */
    public Feature identifyFeatureBySpectrum(Feature feature, IdentificationParams identificationParams) {

        List<IdentificationInfo> identificationInfos = new ArrayList<>();

        for (String libraryId : identificationParams.getLibraryIds()) {
            List<SpectrumDO> spectrumDOS = spectrumService.getByPrecursorMz(feature.getMz() - 0.1, feature.getMz() + 0.1, libraryId);
            if (spectrumDOS.size() == 0) {
                continue;
            }
            Map<Integer, List<SpectrumDO>> spectrumMap = spectrumDOS.stream().collect(Collectors.groupingBy(SpectrumDO::getMsLevel));
            List<SpectrumDO> ms2LibSpectra = spectrumMap.get(2);
            if (ms2LibSpectra == null || ms2LibSpectra.size() == 0) {
                continue;
            }
            for (SpectrumDO spectrumDO : ms2LibSpectra) {
                //PrecursorMz打分
                Double precursorMzScore = Math.abs(spectrumDO.getPrecursorMz() - feature.getMz()) / feature.getMz();

                //谱图相似性打分
                Double similarityScore = 0.0;
                if (feature.getMs2Spectrum() != null) {
                    Spectrum ms2Spectrum = feature.getMs2Spectrum();
                    Spectrum libSpectrum = spectrumDO.getSpectrum();
                    double ms2ForwardScore = spectrumScorer.ms2ForwardScore(ms2Spectrum, libSpectrum, identificationParams.getMzTolerance());
                    double ms2ReverseScore = spectrumScorer.ms2ReverseScore(libSpectrum, ms2Spectrum, identificationParams.getMzTolerance());
                    similarityScore += ms2ForwardScore + ms2ReverseScore;
                }

                //计算总分，两个分数权重一样
                double matchScore = precursorMzScore + similarityScore;

                //鉴定结果填充
                IdentificationInfo identificationInfo = new IdentificationInfo();
                identificationInfo.setCompoundId(spectrumDO.getCompoundId());
                identificationInfo.setSpectrumId(spectrumDO.getSpectrumId());
                identificationInfo.setCompoundName(spectrumDO.getCompoundName());
                identificationInfo.setLibraryName(spectrumDO.getLibraryMembership());
                identificationInfo.setAdduct(spectrumDO.getAdduct());
                identificationInfo.setPrecursorMz(spectrumDO.getPrecursorMz());
                identificationInfo.setSmiles(spectrumDO.getSmiles());
                identificationInfo.setInChI(spectrumDO.getInchI());
                identificationInfo.setUrl(spectrumDO.getUrl());
                identificationInfo.setMatchScore(matchScore);
                identificationInfos.add(identificationInfo);
            }
        }
        identificationInfos.sort(Comparator.comparing(IdentificationInfo::getMatchScore));

        //取分数最大的前几个
        if (identificationInfos.size() >= identificationParams.getTopN()) {
            identificationInfos = identificationInfos.subList(identificationInfos.size() - identificationParams.getTopN(), identificationInfos.size());
        }
        feature.setIdentificationInfos(identificationInfos);

        return feature;
    }

    public Feature identifyFeatureByCompound(Feature feature, List<String> libraryIds) {
        return null;
    }

    public List<Feature> identifyFeaturesBySpectrum(List<Feature> features, List<String> libraryIds) {
        return null;
    }

    public List<Feature> identifyFeaturesByCompound(List<Feature> features, List<String> libraryIds) {
        return null;
    }

}
