package net.csibio.metaphoenix.core.service.impl;

import net.csibio.metaphoenix.client.constants.enums.SpectrumMatchMethod;
import net.csibio.metaphoenix.client.domain.bean.identification.LibraryHit;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryHitServiceImplTest {

    @Mock
    SpectrumService spectrumService;
    @InjectMocks
    LibraryHitServiceImpl libraryHitService;

    private static SpectrumDO spectrum(String id, double[] mzs, double[] ints) {
        SpectrumDO spectrum = new SpectrumDO();
        spectrum.setId(id);
        spectrum.setPrecursorMz(300.0);
        spectrum.setMzs(mzs);
        spectrum.setInts(ints);
        return spectrum;
    }

    private static final SpectrumDO QUERY = spectrum("q", new double[]{100, 150, 200}, new double[]{10, 20, 30});

    @Test
    void bestHitIsReturnedEvenWhenItsScoreIsZero() {
        // no shared peaks with the query, so the cosine score is 0
        SpectrumDO unrelated = spectrum("lib1", new double[]{110, 160, 210}, new double[]{10, 20, 30});
        when(spectrumService.getByPrecursorMz(eq(300.0), anyDouble(), eq("lib"))).thenReturn(List.of(unrelated));

        LibraryHit hit = libraryHitService.getBestHit(QUERY, "lib", 0.01, false, SpectrumMatchMethod.Cosine, false);

        assertNotNull(hit);
        assertEquals("lib1", hit.getLibSpectrumId());
        assertEquals(0.0, hit.getScore(), 1e-9);
    }

    @Test
    void bestHitIsTheHighestScoringSpectrum() {
        SpectrumDO unrelated = spectrum("lib1", new double[]{110, 160, 210}, new double[]{10, 20, 30});
        SpectrumDO identical = spectrum("lib2", new double[]{100, 150, 200}, new double[]{10, 20, 30});
        when(spectrumService.getByPrecursorMz(eq(300.0), anyDouble(), eq("lib"))).thenReturn(List.of(unrelated, identical));

        LibraryHit hit = libraryHitService.getBestHit(QUERY, "lib", 0.01, true, SpectrumMatchMethod.Cosine, false);

        assertEquals("lib2", hit.getLibSpectrumId());
        assertTrue(hit.isDecoy());
        assertEquals(1.0, hit.getScore(), 1e-6);
    }

    @Test
    void noBestHitWithoutCandidates() {
        when(spectrumService.getByPrecursorMz(eq(300.0), anyDouble(), eq("lib"))).thenReturn(List.of());

        assertNull(libraryHitService.getBestHit(QUERY, "lib", 0.01, false, SpectrumMatchMethod.Cosine, false));
    }
}
