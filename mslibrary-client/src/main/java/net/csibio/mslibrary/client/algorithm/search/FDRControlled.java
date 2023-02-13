package net.csibio.mslibrary.client.algorithm.search;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.algorithm.similarity.Similarity;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class FDRControlled {

    @Autowired
    SpectrumService spectrumService;
    @Autowired
    Similarity similarity;

    public HashMap<SpectrumDO, List<LibraryHit>> execute(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO method) {
        log.info("FDRControlled identification progress start on library: " + queryLibraryId + " towards library: " + targetLibraryId + "&" + decoyLibraryId);
        HashMap<SpectrumDO, List<LibraryHit>> resultMap = new HashMap<>();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(queryLibraryId);
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            List<SpectrumDO> targetSpectrumDOS = spectrumService.getByPrecursorMz(spectrumDO.getPrecursorMz(), method.getMzTolerance(), targetLibraryId);
            List<SpectrumDO> decoySpectrumDOS = spectrumService.getByPrecursorMz(spectrumDO.getPrecursorMz(), method.getMzTolerance(), decoyLibraryId);
            List<SpectrumDO> allSpectrumDOS = new ArrayList<>();
            allSpectrumDOS.addAll(targetSpectrumDOS);
            allSpectrumDOS.addAll(decoySpectrumDOS);
            List<LibraryHit> targetHits = new ArrayList<>();
            List<LibraryHit> decoyHits = new ArrayList<>();
            for (SpectrumDO libSpectrum : allSpectrumDOS) {
                double score = 0.0;
                switch (method.getSimilarityType()) {
                    case "Entropy":
                        score = similarity.getEntropySimilarity(spectrumDO.getSpectrum(), libSpectrum.getSpectrum(), method.getMzTolerance());
                    case "Cosine":
                        score = similarity.getDotProduct(spectrumDO.getSpectrum(), libSpectrum.getSpectrum(), method.getMzTolerance());
                    case "Unweighted_Entropy":
                        score = similarity.getUnWeightedEntropySimilarity(spectrumDO.getSpectrum(), libSpectrum.getSpectrum(), method.getMzTolerance());
                }
                LibraryHit libraryHit = new LibraryHit();
                libraryHit.setMatchScore(score);
                libraryHit.setSpectrumId(libSpectrum.getId());
                libraryHit.setPrecursorMz(libSpectrum.getPrecursorMz());
                if (libSpectrum.getLibraryId().equals(targetLibraryId)) {
                    libraryHit.setDecoy(false);
                    targetHits.add(libraryHit);
                } else {
                    libraryHit.setDecoy(true);
                    decoyHits.add(libraryHit);
                }
            }
            List<LibraryHit> libraryHits = new ArrayList<>();
            resultMap.put(spectrumDO, libraryHits);
        });

        return null;
    }

}
