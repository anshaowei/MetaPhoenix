package net.csibio.metaphoenix.client.filter;

import net.csibio.aird.enums.MsLevel;
import net.csibio.metaphoenix.client.constants.enums.IonMode;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.domain.query.SpectrumQuery;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoiseFilterTest {

    private static final String LIB = "lib1";

    @Mock
    SpectrumService spectrumService;
    @InjectMocks
    NoiseFilter noiseFilter;

    /** A spectrum that passes every filter step: 6 strong peaks, one at the precursor m/z. */
    private SpectrumDO good() {
        SpectrumDO s = new SpectrumDO();
        s.setPrecursorMz(500.0);
        s.setMzs(new double[]{100, 200, 300, 400, 450, 500});
        s.setInts(new double[]{100, 90, 80, 70, 60, 50});
        s.setInChIKey("AAAAAAAAAAAAAA-BBBBBBBBBB-N");
        s.setIonMode(IonMode.Positive.getName());
        s.setMsLevel(MsLevel.MS2.getCode());
        return s;
    }

    @SuppressWarnings("unchecked")
    private List<SpectrumDO> runFilter(SpectrumDO... input) {
        when(spectrumService.getAll(any(SpectrumQuery.class), eq(LIB))).thenReturn(new ArrayList<>(List.of(input)));
        noiseFilter.filter(LIB);
        ArgumentCaptor<List<SpectrumDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(spectrumService).insert(captor.capture(), eq(LIB));
        return captor.getValue();
    }

    @Test
    void keepsSpectrumPassingAllChecks() {
        assertEquals(1, runFilter(good()).size());
    }

    @Test
    void removesSpectraWithMissingKeyInformation() {
        SpectrumDO noKey = good();
        noKey.setInChIKey("N/A");
        SpectrumDO noMode = good();
        noMode.setIonMode(null);
        SpectrumDO noPrecursor = good();
        noPrecursor.setPrecursorMz(0d);
        assertTrue(runFilter(noKey, noMode, noPrecursor).isEmpty());
    }

    @Test
    void dropsPeaksBelowOnePercentOfBasePeak() {
        SpectrumDO s = good();
        s.setMzs(new double[]{100, 200, 300, 400, 450, 500, 600});
        s.setInts(new double[]{1000, 900, 800, 700, 600, 500, 9});
        List<SpectrumDO> out = runFilter(s);
        assertEquals(6, out.get(0).getMzs().length);
        assertEquals(6, out.get(0).getInts().length);
    }

    @Test
    void removesLowResolutionSpectra() {
        SpectrumDO s = good();
        s.setMzs(new double[]{100, 200, 300, 400, 450, 500.01}); // 20 ppm away from precursor
        assertTrue(runFilter(s).isEmpty());
    }

    @Test
    void removesSpectraWithFewerThanFiveSignificantPeaks() {
        SpectrumDO s = good();
        s.setInts(new double[]{100, 90, 80, 70, 1.5, 1.5}); // only 4 peaks above 2%... 1.5% are kept at step 2 but not counted
        assertTrue(runFilter(s).isEmpty());
    }

    @Test
    void removesHeavyNonMs2AndNegativeModeSpectra() {
        SpectrumDO heavy = good();
        heavy.setPrecursorMz(1200.0);
        heavy.setMzs(new double[]{100, 200, 300, 400, 450, 1200});
        SpectrumDO heavyMass = good();
        heavyMass.setExactMass(1500.0);
        SpectrumDO ms1 = good();
        ms1.setMsLevel(MsLevel.MS1.getCode());
        SpectrumDO negative = good();
        negative.setIonMode(IonMode.Negative.getName());
        assertTrue(runFilter(heavy, heavyMass, ms1, negative).isEmpty());
    }
}
