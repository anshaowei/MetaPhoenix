package net.csibio.metaphoenix.client.algorithm.identification;

import net.csibio.metaphoenix.client.domain.bean.identification.LibraryHit;
import net.csibio.metaphoenix.client.domain.db.MethodDO;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.service.LibraryHitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentifyTest {

    @Mock
    LibraryHitService libraryHitService;
    @InjectMocks
    Identify identify;

    private static LibraryHit hit(String queryId, double score, boolean decoy) {
        LibraryHit hit = new LibraryHit();
        hit.setQuerySpectrumId(queryId);
        hit.setScore(score);
        hit.setDecoy(decoy);
        return hit;
    }

    private static List<LibraryHit> hits(int count, double score, boolean decoy) {
        List<LibraryHit> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(hit("q" + i, score, decoy));
        }
        return list;
    }

    private HashMap<String, List<LibraryHit>> runIdentify(List<LibraryHit> allHits, double fdr) {
        SpectrumDO query = new SpectrumDO();
        query.setId("q");
        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = new ConcurrentHashMap<>();
        hitsMap.put(query, allHits);
        when(libraryHitService.getTargetDecoyHitsMap(anyList(), eq("target"), eq("decoy"), any())).thenReturn(hitsMap);
        return identify.execute(List.of(query), "target", "decoy", new MethodDO(), fdr);
    }

    @Test
    void findScoreThreshold_returnsLowestThresholdBelowFdr() {
        // 10 targets at 0.9 and 10 decoys at 0.5: from 0.501 upwards only targets remain
        List<LibraryHit> targets = hits(10, 0.9, false);
        List<LibraryHit> decoys = hits(10, 0.5, true);

        OptionalDouble threshold = Identify.findScoreThreshold(targets, decoys, 0.05);

        assertTrue(threshold.isPresent());
        assertEquals(0.501, threshold.getAsDouble(), 1e-9);
    }

    @Test
    void findScoreThreshold_usesDecoyOverTargetPlusDecoy() {
        // 19 targets and 1 decoy at the same score: N_D / (N_T + N_D) = 0.05, N_D / N_T = 0.0526
        List<LibraryHit> targets = hits(19, 0.8, false);
        List<LibraryHit> decoys = hits(1, 0.8, true);

        assertTrue(Identify.findScoreThreshold(targets, decoys, 0.051).isPresent());
        assertTrue(Identify.findScoreThreshold(targets, decoys, 0.05).isEmpty());
    }

    @Test
    void findScoreThreshold_emptyWhenFdrIsNeverReached() {
        // every decoy scores at least as high as every target
        List<LibraryHit> targets = hits(10, 0.6, false);
        List<LibraryHit> decoys = hits(10, 0.9, true);

        assertTrue(Identify.findScoreThreshold(targets, decoys, 0.05).isEmpty());
    }

    @Test
    void findScoreThreshold_emptyWithoutTargetHits() {
        assertTrue(Identify.findScoreThreshold(List.of(), hits(3, 0.9, true), 0.05).isEmpty());
        assertTrue(Identify.findScoreThreshold(List.of(), List.of(), 0.05).isEmpty());
    }

    @Test
    void execute_reportsNoHitsWhenFdrIsNeverReached() {
        List<LibraryHit> allHits = new ArrayList<>(hits(10, 0.6, false));
        allHits.addAll(hits(10, 0.9, true));

        assertTrue(runIdentify(allHits, 0.05).isEmpty());
    }

    @Test
    void execute_reportsOnlyTargetHitsAboveThreshold() {
        List<LibraryHit> allHits = new ArrayList<>();
        allHits.add(hit("q1", 0.95, false));
        allHits.add(hit("q2", 0.90, false));
        allHits.add(hit("q3", 0.40, false));
        allHits.add(hit("d1", 0.50, true));

        HashMap<String, List<LibraryHit>> result = runIdentify(allHits, 0.05);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("q1"));
        assertTrue(result.containsKey("q2"));
        result.values().forEach(list -> list.forEach(h -> assertFalse(h.isDecoy())));
    }
}
