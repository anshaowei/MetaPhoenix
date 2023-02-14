package net.csibio.mslibrary.client.algorithm.search;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.algorithm.similarity.Similarity;
import net.csibio.mslibrary.client.constants.Constants;
import net.csibio.mslibrary.client.constants.enums.DecoyProcedure;
import net.csibio.mslibrary.client.constants.enums.SpectrumMatchMethod;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class FDRControlled {

    @Autowired
    SpectrumService spectrumService;
    @Autowired
    Similarity similarity;

    public void execute(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO method) {

        //initiate
        log.info("FDRControlled identification progress start on library: " + queryLibraryId + " towards library: " + targetLibraryId + "&" + decoyLibraryId);
        ConcurrentHashMap<String, List<LibraryHit>> allHitsMap = new ConcurrentHashMap<>();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(queryLibraryId);

        //process each spectrum to get all hits
        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            double mzTolerance = method.getPpmForMzTolerance() ? method.getPpm() * spectrumDO.getPrecursorMz() * Constants.PPM : method.getMzTolerance();
            List<SpectrumDO> targetSpectrumDOS = spectrumService.getByPrecursorMz(spectrumDO.getPrecursorMz(), mzTolerance, targetLibraryId);
            List<SpectrumDO> decoySpectrumDOS = spectrumService.getByPrecursorMz(spectrumDO.getPrecursorMz(), mzTolerance, decoyLibraryId);
            List<SpectrumDO> allSpectrumDOS = new ArrayList<>();
            allSpectrumDOS.addAll(targetSpectrumDOS);
            allSpectrumDOS.addAll(decoySpectrumDOS);
            List<LibraryHit> targetHits = new ArrayList<>();
            List<LibraryHit> decoyHits = new ArrayList<>();
            List<LibraryHit> allHits = new ArrayList<>();
            for (SpectrumDO libSpectrum : allSpectrumDOS) {
                SpectrumMatchMethod spectrumMatchMethod = SpectrumMatchMethod.valueOf(method.getSpectrumMatchMethod());
                double score = switch (spectrumMatchMethod) {
                    case Entropy ->
                            similarity.getEntropySimilarity(spectrumDO.getSpectrum(), libSpectrum.getSpectrum(), mzTolerance);
                    case Cosine ->
                            similarity.getDotProduct(spectrumDO.getSpectrum(), libSpectrum.getSpectrum(), mzTolerance);
                    case Unweighted_Entropy ->
                            similarity.getUnWeightedEntropySimilarity(spectrumDO.getSpectrum(), libSpectrum.getSpectrum(), mzTolerance);
                };
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
            allHits.addAll(targetHits);
            allHits.addAll(decoyHits);
            allHitsMap.put(spectrumDO.getId(), allHits);
        });

        //process with different strategies except STDS
        DecoyProcedure decoyProcedure = DecoyProcedure.valueOf(method.getDecoyProcedure());
        switch (decoyProcedure) {
            case CTDC -> allHitsMap.keySet().parallelStream().forEach(spectrumId -> {
                List<LibraryHit> allHits = allHitsMap.get(spectrumId);
                if (allHits.size() > 0) {
                    allHits.sort(Comparator.comparing(LibraryHit::getMatchScore).reversed());
                    allHitsMap.put(spectrumId, Collections.singletonList(allHits.get(0)));
                }
            });
            case TTDC -> allHitsMap.keySet().parallelStream().forEach(spectrumId -> {
                List<LibraryHit> allHits = allHitsMap.get(spectrumId);
                if (allHits.size() > 0) {
                    allHits.sort(Comparator.comparing(LibraryHit::getMatchScore).reversed());
                    if (allHits.get(0).isDecoy()) {
                        allHitsMap.put(spectrumId, new ArrayList<>());
                    } else {
                        allHitsMap.put(spectrumId, Collections.singletonList(allHits.get(0)));
                    }
                }
            });
            case Common -> allHitsMap.keySet().parallelStream().forEach(spectrumId -> {
                List<LibraryHit> allHits = allHitsMap.get(spectrumId);
                allHits.removeIf(libraryHit -> libraryHit.isDecoy() && libraryHit.getMatchScore() > method.getThreshold());
            });
            case STDS -> {
            }
            default -> log.error("Decoy procedure not supported: " + decoyProcedure);
        }

        //process STDS strategy


    }

}
