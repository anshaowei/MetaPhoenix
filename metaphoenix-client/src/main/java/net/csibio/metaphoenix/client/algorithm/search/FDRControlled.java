package net.csibio.metaphoenix.client.algorithm.search;

import lombok.extern.slf4j.Slf4j;
import net.csibio.metaphoenix.client.constants.enums.DecoyProcedure;
import net.csibio.metaphoenix.client.domain.bean.identification.LibraryHit;
import net.csibio.metaphoenix.client.domain.db.MethodDO;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.service.LibraryHitService;
import net.csibio.metaphoenix.client.service.SpectrumService;
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
    LibraryHitService libraryHitService;

    public void execute(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO methodDO) {

        //initiate
        log.info("FDRControlled identification progress start on library: " + queryLibraryId + " towards library: " + targetLibraryId + "&" + decoyLibraryId);
        List<SpectrumDO> querySpectrumDOS = spectrumService.getAllByLibraryId(queryLibraryId);
        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> allHitsMap = libraryHitService.getTargetDecoyHitsMap(querySpectrumDOS, targetLibraryId, decoyLibraryId, methodDO);

        //process with different strategies except STDS
        DecoyProcedure decoyProcedure = DecoyProcedure.valueOf(methodDO.getDecoyProcedure());
        switch (decoyProcedure) {
            case CTDC -> allHitsMap.keySet().parallelStream().forEach(spectrumDO -> {
                List<LibraryHit> allHits = allHitsMap.get(spectrumDO);
                if (allHits.size() > 0) {
                    allHits.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                    allHitsMap.put(spectrumDO, Collections.singletonList(allHits.get(0)));
                }
            });
            case TTDC -> allHitsMap.keySet().parallelStream().forEach(spectrumDO -> {
                List<LibraryHit> allHits = allHitsMap.get(spectrumDO);
                if (allHits.size() > 0) {
                    allHits.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                    if (allHits.get(0).isDecoy()) {
                        allHitsMap.put(spectrumDO, new ArrayList<>());
                    } else {
                        allHitsMap.put(spectrumDO, Collections.singletonList(allHits.get(0)));
                    }
                }
            });
            case Common -> allHitsMap.keySet().parallelStream().forEach(spectrumDO -> {
                List<LibraryHit> allHits = allHitsMap.get(spectrumDO);
                allHits.removeIf(libraryHit -> libraryHit.isDecoy() && libraryHit.getScore() > methodDO.getThreshold());
            });
            case STDS -> {
            }
            default -> log.error("Decoy procedure not supported: " + decoyProcedure);
        }

        //process STDS strategy

    }

}
