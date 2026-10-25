package net.csibio.metaphoenix.client.algorithm.search;

import net.csibio.metaphoenix.client.domain.bean.identification.LibraryHit;
import net.csibio.metaphoenix.client.domain.db.MethodDO;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.service.LibraryHitService;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FDRControlledTest {

    @Mock
    SpectrumService spectrumService;
    @Mock
    LibraryHitService libraryHitService;
    @InjectMocks
    FDRControlled fdrControlled;

    private static LibraryHit hit(String id, double score, boolean decoy) {
        LibraryHit h = new LibraryHit();
        h.setLibSpectrumId(id);
        h.setScore(score);
        h.setDecoy(decoy);
        return h;
    }

    /** Runs execute() on one query spectrum with the given hits and returns the hits left afterwards. */
    private List<LibraryHit> run(String procedure, Double threshold, LibraryHit... hits) {
        SpectrumDO query = new SpectrumDO();
        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> map = new ConcurrentHashMap<>();
        map.put(query, new ArrayList<>(List.of(hits)));
        MethodDO method = new MethodDO();
        method.setDecoyProcedure(procedure);
        method.setThreshold(threshold);
        when(spectrumService.getAllByLibraryId("q")).thenReturn(List.of(query));
        when(libraryHitService.getTargetDecoyHitsMap(any(List.class), eq("t"), eq("d"), eq(method))).thenReturn(map);

        fdrControlled.execute("q", "t", "d", method);
        return map.get(query);
    }

    @Test
    void ctdcKeepsOnlyTheBestHitEvenWhenItIsDecoy() {
        List<LibraryHit> out = run("CTDC", null, hit("t1", 0.7, false), hit("d1", 0.9, true), hit("t2", 0.8, false));
        assertEquals(1, out.size());
        assertEquals("d1", out.get(0).getLibSpectrumId());
    }

    @Test
    void ttdcKeepsBestTargetHit() {
        List<LibraryHit> out = run("TTDC", null, hit("t1", 0.9, false), hit("d1", 0.5, true));
        assertEquals(1, out.size());
        assertEquals("t1", out.get(0).getLibSpectrumId());
    }

    @Test
    void ttdcDiscardsSpectrumWhenBestHitIsDecoy() {
        assertTrue(run("TTDC", null, hit("t1", 0.5, false), hit("d1", 0.9, true)).isEmpty());
    }

    @Test
    void commonRemovesOnlyDecoyHitsAboveThreshold() {
        List<LibraryHit> out = run("Common", 0.6,
                hit("t1", 0.9, false), hit("d1", 0.8, true), hit("d2", 0.4, true));
        assertEquals(List.of("t1", "d2"), out.stream().map(LibraryHit::getLibSpectrumId).toList());
    }

    @Test
    void stdsLeavesHitsUntouched() {
        assertEquals(2, run("STDS", null, hit("t1", 0.9, false), hit("d1", 0.8, true)).size());
    }
}
