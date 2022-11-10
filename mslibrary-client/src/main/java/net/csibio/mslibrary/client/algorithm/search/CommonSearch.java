package net.csibio.mslibrary.client.algorithm.search;

import io.github.msdk.io.mgf.MgfFileImportMethod;
import io.github.msdk.io.mgf.MgfMsSpectrum;
import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.bean.common.Spectrum;
import net.csibio.aird.enums.MsLevel;
import net.csibio.mslibrary.client.algorithm.score.SpectrumScorer;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.bean.params.IdentificationParams;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CommonSearch {

    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;
    @Autowired
    SpectrumScorer spectrumScorer;

    public void identify(String filePath, IdentificationParams identificationParams) {
        //1.文件解析
        List<MgfMsSpectrum> mgfMsSpectrumList = new ArrayList<>();
        try {
            File file = new File(filePath);
            MgfFileImportMethod mgfFileImportMethod = new MgfFileImportMethod(file);
            mgfFileImportMethod.execute();
            mgfMsSpectrumList = mgfFileImportMethod.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //2.搜索本地数据库
        ConcurrentHashMap<MgfMsSpectrum, List<LibraryHit>> resultMap = new ConcurrentHashMap<>();
        mgfMsSpectrumList.parallelStream().forEach(mgfMsSpectrum -> {
            Double precursorMz = mgfMsSpectrum.getPrecursorMass();
            if (mgfMsSpectrum.getPrecursorCharge() != null && mgfMsSpectrum.getPrecursorCharge() != 0) {
                precursorMz = mgfMsSpectrum.getPrecursorMass() / mgfMsSpectrum.getPrecursorCharge();
            }
            List<SpectrumDO> spectrumDOS = new ArrayList<>();
            for (String libraryId : identificationParams.getLibraryIds()) {
                SpectrumQuery spectrumQuery = new SpectrumQuery();
                spectrumQuery.setPrecursorMz(precursorMz);
                spectrumQuery.setMzTolerance(identificationParams.getMzTolerance());
                spectrumQuery.setLibraryId(libraryId);
                spectrumQuery.setMsLevel(MsLevel.MS2.getCode());
                spectrumDOS.addAll(spectrumService.getAll(spectrumQuery, libraryId));
            }

            //对MS2谱图的匹配程度进行打分
            List<LibraryHit> libraryHits = new ArrayList<>();
            for (SpectrumDO spectrumDO : spectrumDOS) {
                Double similarityScore = 0.0;
                double[] intensityArray = new double[mgfMsSpectrum.getIntensityValues().length];
                for (int i = 0; i < mgfMsSpectrum.getIntensityValues().length; i++) {
                    intensityArray[i] = mgfMsSpectrum.getIntensityValues()[i];
                }
                Spectrum ms2Spectrum = new Spectrum(mgfMsSpectrum.getMzValues(), intensityArray);
                Spectrum libSpectrum = new Spectrum(spectrumDO.getMzs(), spectrumDO.getInts());
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
            libraryHits.sort(Comparator.comparing(LibraryHit::getMatchScore).reversed());
            if (libraryHits.size() >= identificationParams.getTopN()) {
                libraryHits = libraryHits.subList(0, identificationParams.getTopN());
            }

            //3.输出标准结果
            resultMap.put(mgfMsSpectrum, libraryHits);
        });

    }
}
