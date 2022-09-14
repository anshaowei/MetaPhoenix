package net.csibio.mslibrary.client.algorithm.search;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.algorithm.score.SpectrumScorer;
import net.csibio.mslibrary.client.domain.bean.identification.Feature;
import net.csibio.mslibrary.client.domain.bean.identification.IdentificationInfo;
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

    public Feature identifyFeature(Feature feature, List<String> libraryIds) {
        //根据谱图进行匹配
        Double mzTolerance = 0.1;
        List<IdentificationInfo> identificationInfos = new ArrayList<>();
        for (String libraryId : libraryIds) {
            List<SpectrumDO> spectrumDOS = spectrumService.getByPrecursorMz(feature.getMz() - 0.1, feature.getMz() + 0.1, libraryId);
            if (spectrumDOS.size() == 0) {
                continue;
            }
            Map<Integer, List<SpectrumDO>> spectrumMap = spectrumDOS.stream().collect(Collectors.groupingBy(SpectrumDO::getMsLevel));
            List<SpectrumDO> ms2LibSpectra = spectrumMap.get(2);
            for (SpectrumDO spectrumDO : ms2LibSpectra) {
                //PrecursorMz打分
                Double precursorMzScore = Math.abs(spectrumDO.getPrecursorMz() - feature.getMz()) / feature.getMz();

                //谱图相似性打分
                Double similarityScore = 0.0;
                if (feature.getMs2Spectrum() != null) {
                    Spectrum ms2Spectrum = feature.getMs2Spectrum();
                    Spectrum libSpectrum = spectrumDO.getSpectrum();
                    double ms2ForwardScore = spectrumScorer.ms2ForwardScore(ms2Spectrum, libSpectrum, mzTolerance);
                    double ms2ReverseScore = spectrumScorer.ms2ReverseScore(libSpectrum, ms2Spectrum, mzTolerance);
                    similarityScore += ms2ForwardScore + ms2ReverseScore;
                }

                //计算总分
                double totalScore = precursorMzScore + similarityScore;

                //鉴定结果填充
                IdentificationInfo identificationInfo = new IdentificationInfo();
                identificationInfo.setCompoundId(spectrumDO.getCompoundId());
                identificationInfo.setCompoundName(spectrumDO.getCompoundName());
                identificationInfo.setMatchScore(totalScore);
                identificationInfo.setSmiles(spectrumDO.getSmiles());
                identificationInfo.setInChI(spectrumDO.getInchI());
                identificationInfos.add(identificationInfo);
            }
        }
        identificationInfos.sort(Comparator.comparing(IdentificationInfo::getMatchScore));

        //取前10个
        if (identificationInfos.size() > 10) {
            identificationInfos = identificationInfos.subList(identificationInfos.size() - 10, identificationInfos.size() - 1);
        }
        feature.setIdentificationInfos(identificationInfos);

        return feature;
    }

    public List<Feature> identifyFeatures(List<Feature> features, List<String> libraryIds) {
        features.stream().forEach(feature -> {
            identifyFeature(feature, libraryIds);
        });
        return features;
    }

}
