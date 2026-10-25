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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static MethodDO method(DecoyStrategy strategy, long seed) {
        MethodDO method = method(strategy);
        method.setRandomSeed(seed);
        return method;
    }

    private List<SpectrumDO> captureInserted() {
        return captureInserted(DecoyStrategy.IonEntropyBased);
    }

    @SuppressWarnings("unchecked")
    private List<SpectrumDO> captureInserted(DecoyStrategy strategy) {
        ArgumentCaptor<List<SpectrumDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(spectrumService, atLeastOnce()).insert(captor.capture(), eq(LIB + "-" + strategy.getName()));
        return captor.getValue();
    }

    /** Generate decoys for the given library and return them by the id of their target spectrum. */
    private Map<String, SpectrumDO> generate(List<SpectrumDO> library, MethodDO method) {
        reset(spectrumService);
        when(spectrumService.exists(any(SpectrumQuery.class), anyString())).thenReturn(false);
        when(spectrumService.getAllByLibraryId(LIB)).thenReturn(library);
        spectrumGenerator.execute(LIB, method);
        DecoyStrategy strategy = DecoyStrategy.valueOf(method.getDecoyStrategy());
        return captureInserted(strategy).stream().collect(Collectors.toMap(SpectrumDO::getRawSpectrumId, Function.identity()));
    }

    /** Ten spectra with ten peaks each in two precursor groups, large enough that two seeds practically never agree. */
    private static List<SpectrumDO> largerLibrary() {
        List<SpectrumDO> library = new ArrayList<>();
        for (int s = 0; s < 10; s++) {
            double precursorMz = s < 5 ? 400.0 : 600.0;
            double[] mzs = new double[10];
            double[] ints = new double[10];
            for (int i = 0; i < 9; i++) {
                mzs[i] = 50 + 30 * i + (s % 3) * 0.5;
                ints[i] = 10 + ((s * 7 + i * 13) % 90);
            }
            mzs[9] = precursorMz;
            ints[9] = 100;
            library.add(spectrum("s" + s, precursorMz, mzs, ints));
        }
        return library;
    }

    private static void assertSameDecoys(Map<String, SpectrumDO> expected, Map<String, SpectrumDO> actual) {
        assertEquals(expected.keySet(), actual.keySet());
        expected.forEach((id, decoy) -> {
            assertArrayEquals(decoy.getMzs(), actual.get(id).getMzs(), "mzs of decoy for " + id);
            assertArrayEquals(decoy.getInts(), actual.get(id).getInts(), "intensities of decoy for " + id);
        });
    }

    private static boolean sameDecoys(Map<String, SpectrumDO> a, Map<String, SpectrumDO> b) {
        return a.keySet().stream().allMatch(id -> Arrays.equals(a.get(id).getMzs(), b.get(id).getMzs())
                && Arrays.equals(a.get(id).getInts(), b.get(id).getInts()));
    }

    private static final List<DecoyStrategy> SUPPORTED = List.of(DecoyStrategy.Naive, DecoyStrategy.XYMeta,
            DecoyStrategy.SpectralEntropyBased, DecoyStrategy.SpectralEntropyBased_PR,
            DecoyStrategy.IonEntropyBased, DecoyStrategy.IonEntropyBased_PR);

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

    @Test
    void sameSeedGivesSameDecoysForEveryStrategy() {
        for (DecoyStrategy strategy : SUPPORTED) {
            Map<String, SpectrumDO> first = generate(largerLibrary(), method(strategy, 42));
            Map<String, SpectrumDO> second = generate(largerLibrary(), method(strategy, 42));
            assertEquals(10, first.size(), strategy.getName());
            assertSameDecoys(first, second);
        }
    }

    @Test
    void differentSeedsGiveDifferentDecoys() {
        for (DecoyStrategy strategy : SUPPORTED) {
            Map<String, SpectrumDO> first = generate(largerLibrary(), method(strategy, 1));
            Map<String, SpectrumDO> second = generate(largerLibrary(), method(strategy, 2));
            assertFalse(sameDecoys(first, second), strategy.getName());
        }
    }

    @Test
    void seededDecoysDoNotDependOnProcessingOrder() {
        List<SpectrumDO> reversed = largerLibrary();
        Collections.reverse(reversed);

        assertSameDecoys(generate(largerLibrary(), method(DecoyStrategy.SpectralEntropyBased, 7)),
                generate(reversed, method(DecoyStrategy.SpectralEntropyBased, 7)));
    }

    @Test
    void spectralEntropyDecoysKeepMzsAndPermuteIntensities() {
        List<SpectrumDO> library = largerLibrary();
        Map<String, SpectrumDO> decoys = generate(library, method(DecoyStrategy.SpectralEntropyBased, 3));
        for (SpectrumDO target : library) {
            SpectrumDO decoy = decoys.get(target.getId());
            assertArrayEquals(target.getMzs(), decoy.getMzs());
            assertArrayEquals(sorted(target.getInts()), sorted(decoy.getInts()));
        }
    }

    @Test
    void precursorReservedStrategiesKeepThePrecursorPeak() {
        for (DecoyStrategy strategy : List.of(DecoyStrategy.SpectralEntropyBased_PR, DecoyStrategy.IonEntropyBased_PR)) {
            List<SpectrumDO> library = largerLibrary();
            Map<String, SpectrumDO> decoys = generate(library, method(strategy, 5));
            for (SpectrumDO target : library) {
                SpectrumDO decoy = decoys.get(target.getId());
                int index = Arrays.binarySearch(decoy.getMzs(), target.getPrecursorMz());
                assertTrue(index >= 0, strategy.getName() + " keeps the precursor m/z");
                assertEquals(100, decoy.getInts()[index], strategy.getName() + " keeps the precursor intensity");
                assertArrayEquals(sorted(target.getInts()), sorted(decoy.getInts()), strategy.getName());
            }
        }
    }

    @Test
    void naiveAndXyMetaDecoysHaveTheTargetPeakCount() {
        for (DecoyStrategy strategy : List.of(DecoyStrategy.Naive, DecoyStrategy.XYMeta)) {
            List<SpectrumDO> library = largerLibrary();
            Map<String, SpectrumDO> decoys = generate(library, method(strategy, 9));
            for (SpectrumDO target : library) {
                SpectrumDO decoy = decoys.get(target.getId());
                assertEquals(target.getMzs().length, decoy.getMzs().length, strategy.getName());
                assertTrue(decoy.isDecoy());
            }
        }
    }
}
