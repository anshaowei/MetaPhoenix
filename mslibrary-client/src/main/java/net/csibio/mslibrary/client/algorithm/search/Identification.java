package net.csibio.mslibrary.client.algorithm.search;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.algorithm.score.SpectrumScorer;
import net.csibio.mslibrary.client.domain.bean.identification.Feature;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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

//        //根据化合物进行匹配
//        for (String libraryId : libraryIds) {
//            //获取及组装数据
//            List<CompoundDO> compoundDOS = compoundService.getAllByLibraryId(libraryId);
//            TreeSet<SimpleCompound> simpleCompounds = new TreeSet<>();
//            for (CompoundDO compoundDO : compoundDOS) {
//                for (Adduct adduct : compoundDO.getAdducts()) {
//                    SimpleCompound simpleCompound = new SimpleCompound();
//                    simpleCompound.setMz((compoundDO.getMonoMw() + adduct.getMw()) / adduct.getCharge());
//                    simpleCompound.setCompoundId(compoundDO.getId());
//                    simpleCompound.setAdduct(adduct);
//                    simpleCompounds.add(simpleCompound);
//                }
//            }
//            NavigableSet<SimpleCompound> subSet = simpleCompounds.subSet(new SimpleCompound(feature.getMz() - 0.1), true,
//                    new SimpleCompound(feature.getMz() + 0.1), true);
//        }

        //根据谱图进行匹配
        Double mzTolerance = 0.1;
        for (String libraryId : libraryIds) {
            List<SpectrumDO> spectrumDOS = spectrumService.getByPrecursorMz(feature.getMz() - 0.1, feature.getMz() + 0.1, libraryId);
            for (SpectrumDO spectrumDO : spectrumDOS) {

                //PrecursorMz打分
                Double precursorMzScore = Math.abs(spectrumDO.getPrecursorMz() - feature.getMz()) / feature.getMz();

                //谱图相似性打分
                Spectrum ms1Spectrum = feature.getMs1Spectrum();
                Spectrum ms2Spectrum = feature.getMs2Spectrum();
                Spectrum libSpectrum = spectrumDO.getSpectrum();
                double ms1ForwardScore = spectrumScorer.ms1ForwardScore(ms1Spectrum, libSpectrum, mzTolerance);
                double ms1ReverseScore = spectrumScorer.ms1ReverseScore(libSpectrum, ms1Spectrum, mzTolerance);
                double ms2ForwardScore = spectrumScorer.ms2ForwardScore(ms2Spectrum, libSpectrum, mzTolerance);
                double ms2ReverseScore = spectrumScorer.ms2ReverseScore(libSpectrum, ms2Spectrum, mzTolerance);
                double similarityScore = (ms1ForwardScore + ms1ReverseScore + ms2ForwardScore + ms2ReverseScore) / 4;



            }
        }

        return feature;
    }

}
