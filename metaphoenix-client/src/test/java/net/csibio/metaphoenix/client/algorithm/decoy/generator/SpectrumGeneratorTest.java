package net.csibio.metaphoenix.client.algorithm.decoy.generator;

import net.csibio.metaphoenix.client.constants.enums.DecoyStrategy;
import net.csibio.metaphoenix.client.domain.db.MethodDO;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.domain.query.SpectrumQuery;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpectrumGeneratorTest {

    private static final String LIB = "lib1";

    @Mock
    SpectrumService spectrumService;
    @InjectMocks
    SpectrumGenerator spectrumGenerator;

    private static MethodDO method(DecoyStrategy strategy) {
        MethodDO method = new MethodDO();
        method.setPpm(10);
        method.setPpmForMzTolerance(true);
        method.setDecoyStrategy(strategy.getName());
        return method;
    }

    private static SpectrumDO spectrum(String id, double precursorMz, double[] mzs, double[] ints) {
        SpectrumDO spectrum = new SpectrumDO();
        spectrum.setId(id);
        spectrum.setPrecursorMz(precursorMz);
        spectrum.setMzs(mzs);
        spectrum.setInts(ints);
        return spectrum;
    }

    /** Three spectra sharing a precursor m/z, so ion entropies are computed across them. */
    private static List<SpectrumDO> library() {
        return List.of(
                spectrum("s1", 300.0, new double[]{100, 150, 200, 250, 300}, new double[]{10, 20, 30, 40, 100}),
                spectrum("s2", 300.0, new double[]{100, 150, 210, 250, 300}, new double[]{15, 25, 35, 45, 100}),
                spectrum("s3", 300.0, new double[]{100, 160, 220, 260, 300}, new double[]{5, 50, 60, 70, 100}));
    }

    @SuppressWarnings("unchecked")
    private List<SpectrumDO> captureInserted() {
        ArgumentCaptor<List<SpectrumDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(spectrumService).insert(captor.capture(), eq(LIB + "-" + DecoyStrategy.IonEntropyBased.getName()));
        return captor.getValue();
    }

    private static double[] sorted(double[] values) {
        double[] copy = values.clone();
        Arrays.sort(copy);
        return copy;
    }

    @Test
    void existingDecoyLibraryIsKeptWithoutOverwrite() {
        when(spectrumService.exists(any(SpectrumQuery.class), eq("lib1-IonEntropyBased"))).thenReturn(true);

        spectrumGenerator.execute(LIB, method(DecoyStrategy.IonEntropyBased));

        verify(spectrumService, never()).getAllByLibraryId(anyString());
        verify(spectrumService, never()).remove(any(SpectrumQuery.class), anyString());
        verify(spectrumService, never()).insert(anyList(), anyString());
    }

    @Test
    void existingDecoyLibraryIsReplacedWithOverwrite() {
        when(spectrumService.exists(any(SpectrumQuery.class), eq("lib1-IonEntropyBased"))).thenReturn(true);
        when(spectrumService.getAllByLibraryId(LIB)).thenReturn(library());

        spectrumGenerator.execute(LIB, method(DecoyStrategy.IonEntropyBased), true);

        InOrder inOrder = inOrder(spectrumService);
        inOrder.verify(spectrumService).remove(any(SpectrumQuery.class), eq("lib1-IonEntropyBased"));
        inOrder.verify(spectrumService).insert(anyList(), eq("lib1-IonEntropyBased"));
        assertEquals(3, captureInserted().size());
    }

    @Test
    void newDecoyLibraryIsInsertedWithoutRemoving() {
        when(spectrumService.exists(any(SpectrumQuery.class), eq("lib1-IonEntropyBased"))).thenReturn(false);
        when(spectrumService.getAllByLibraryId(LIB)).thenReturn(library());

        spectrumGenerator.execute(LIB, method(DecoyStrategy.IonEntropyBased));

        verify(spectrumService, never()).remove(any(SpectrumQuery.class), anyString());
        assertEquals(3, captureInserted().size());
    }

    @Test
    void unsupportedStrategyKeepsExistingDecoyLibrary() {
        when(spectrumService.exists(any(SpectrumQuery.class), eq("lib1-FragmentationTreeBased"))).thenReturn(true);
        when(spectrumService.getAllByLibraryId(LIB)).thenReturn(library());

        spectrumGenerator.execute(LIB, method(DecoyStrategy.FragmentationTreeBased), true);

        verify(spectrumService, never()).remove(any(SpectrumQuery.class), anyString());
        verify(spectrumService, never()).insert(anyList(), anyString());
    }

    @Test
    void ionEntropyDecoysKeepTargetMzsAndPermuteIntensities() {
        when(spectrumService.exists(any(SpectrumQuery.class), anyString())).thenReturn(false);
        List<SpectrumDO> library = library();
        when(spectrumService.getAllByLibraryId(LIB)).thenReturn(library);

        spectrumGenerator.execute(LIB, method(DecoyStrategy.IonEntropyBased));

        List<SpectrumDO> decoys = captureInserted();
        for (SpectrumDO target : library) {
            SpectrumDO decoy = decoys.stream().filter(d -> target.getId().equals(d.getRawSpectrumId())).findFirst().orElseThrow();
            assertTrue(decoy.isDecoy());
            assertEquals(LIB + "-IonEntropyBased", decoy.getLibraryId());
            assertEquals(target.getPrecursorMz(), decoy.getPrecursorMz());
            assertArrayEquals(target.getMzs(), decoy.getMzs());
            assertArrayEquals(sorted(target.getInts()), sorted(decoy.getInts()));
        }
    }
}
