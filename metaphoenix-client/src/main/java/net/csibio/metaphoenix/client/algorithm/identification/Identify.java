package net.csibio.metaphoenix.client.algorithm.identification;

import lombok.extern.slf4j.Slf4j;
import net.csibio.metaphoenix.client.domain.bean.identification.LibraryHit;
import net.csibio.metaphoenix.client.domain.db.MethodDO;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.service.LibraryHitService;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component("identify")
@Slf4j
public class Identify {

    @Autowired
    LibraryHitService libraryHitService;
    @Autowired
    SpectrumService spectrumService;

    public HashMap<String, List<LibraryHit>> execute(List<SpectrumDO> querySpectrumDOS, String targetLibraryId, String decoyLibraryId, MethodDO methodDO, double fdr) {
        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = libraryHitService.getTargetDecoyHitsMap(querySpectrumDOS, targetLibraryId, decoyLibraryId, methodDO);
        List<LibraryHit> decoyHits = new ArrayList<>();
        List<LibraryHit> targetHits = new ArrayList<>();
        hitsMap.forEach((k, v) -> {
            if (v.size() != 0) {
                //separated target-decoy competition
                Map<Boolean, List<LibraryHit>> decoyTargetMap = v.stream().collect(Collectors.groupingBy(LibraryHit::isDecoy));
                List<LibraryHit> targetHitsList = decoyTargetMap.get(false);
                List<LibraryHit> decoyHitsList = decoyTargetMap.get(true);
                if (targetHitsList != null && targetHitsList.size() != 0) {
                    targetHits.addAll(targetHitsList);
                }
                if (decoyHitsList != null && decoyHitsList.size() != 0) {
                    decoyHits.addAll(decoyHitsList);
                }
            }
        });

        //find score threshold in accordance with the given fdr
        double scoreThreshold = 0;
        int steps = 1000;
        for (int i = 0; i < steps; i++) {
            double threshold = (double) i / steps;
            int targetCount, decoyCount;
            targetCount = targetHits.stream().filter(hit -> hit.getScore() >= threshold).toList().size();
            decoyCount = decoyHits.stream().filter(hit -> hit.getScore() >= threshold).toList().size();
            double estimatedFDR = 0.5;
            if (targetCount != 0) {
                estimatedFDR = (double) decoyCount / (targetCount + decoyCount);
            }
            if (estimatedFDR < fdr) {
                scoreThreshold = threshold;
                break;
            }
        }

        //get hits with this score threshold
        final double finalScoreThreshold = scoreThreshold;
        List<LibraryHit> resultHits = targetHits.stream().filter(hit -> hit.getScore() >= finalScoreThreshold).toList();
        HashMap<String, List<LibraryHit>> resultMap = (HashMap<String, List<LibraryHit>>) resultHits.stream().collect(Collectors.groupingBy(LibraryHit::getQuerySpectrumId));
        return resultMap;
    }

    public void execute(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO methodDO, double fdr) {
        execute(spectrumService.getAllByLibraryId(queryLibraryId), targetLibraryId, decoyLibraryId, methodDO, fdr);
    }

}
