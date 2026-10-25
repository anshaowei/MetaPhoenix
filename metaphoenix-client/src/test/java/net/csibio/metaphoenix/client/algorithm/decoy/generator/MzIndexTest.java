package net.csibio.metaphoenix.client.algorithm.decoy.generator;

import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.utils.ArrayUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class MzIndexTest {

    private static SpectrumDO spectrum(String id, double precursorMz, double... mzs) {
        SpectrumDO spectrum = new SpectrumDO();
        spectrum.setId(id);
        spectrum.setPrecursorMz(precursorMz);
        spectrum.setMzs(mzs);
        return spectrum;
    }

    /** Spectra with precursors and peaks on a coarse grid, so that many m/z values are equal or very close. */
    private static List<SpectrumDO> randomLibrary(Random random, int size) {
        List<SpectrumDO> library = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double precursorMz = 100 + random.nextInt(200) * 0.5 + random.nextInt(3) * 0.0005;
            double[] mzs = new double[1 + random.nextInt(10)];
            for (int k = 0; k < mzs.length; k++) {
                mzs[k] = 50 + random.nextInt(400) * 0.5 + random.nextInt(3) * 0.0005;
            }
            java.util.Arrays.sort(mzs);
            library.add(spectrum("s" + i, precursorMz, mzs));
        }
        return library;
    }

    @Test
    void precursorIndexFindsTheSameSpectraAsAFullScanInLibraryOrder() {
        Random random = new Random(1);
        List<SpectrumDO> library = randomLibrary(random, 2000);
        MzIndex index = MzIndex.ofPrecursors(library);
        for (int q = 0; q < 500; q++) {
            double mz = library.get(random.nextInt(library.size())).getPrecursorMz() + (random.nextInt(3) - 1) * 0.0004;
            double tolerance = 10 * 1e-6 * mz;
            Predicate<SpectrumDO> condition = spectrum -> Math.abs(spectrum.getPrecursorMz() - mz) < tolerance;

            assertEquals(library.stream().filter(condition).toList(), index.find(mz, tolerance, condition));
        }
    }

    @Test
    void peakIndexFindsTheSameSpectraAsAFullScanInLibraryOrder() {
        Random random = new Random(2);
        List<SpectrumDO> library = randomLibrary(random, 2000);
        MzIndex index = MzIndex.ofPeaks(library);
        for (int q = 0; q < 500; q++) {
            double mz = 50 + random.nextInt(400) * 0.5 + (random.nextInt(3) - 1) * 0.0005;
            double tolerance = 10 * 1e-6 * mz;
            Predicate<SpectrumDO> condition = spectrum -> ArrayUtil.findNearestDiff(spectrum.getMzs(), mz) < tolerance;

            assertEquals(library.stream().filter(condition).toList(), index.find(mz, tolerance, condition));
        }
    }

    @Test
    void spectraAtTheWindowEdgeAreFound() {
        // 500 and 500.004999 are within 10 ppm (0.005) of each other, 500.005001 is not
        List<SpectrumDO> library = List.of(spectrum("a", 500.004999), spectrum("b", 500.0), spectrum("c", 500.005001), spectrum("d", 499.995001));
        MzIndex index = MzIndex.ofPrecursors(library);
        Predicate<SpectrumDO> condition = spectrum -> Math.abs(spectrum.getPrecursorMz() - 500.0) < 0.005;

        assertEquals(List.of(library.get(0), library.get(1), library.get(3)), index.find(500.0, 0.005, condition));
    }

    @Test
    void spectraThatCannotBeIndexedAreScannedInstead() {
        // a negative m/z cannot be indexed, so the whole library is scanned with the condition
        List<SpectrumDO> library = List.of(spectrum("a", 200.0), spectrum("b", -1.0), spectrum("c", 200.001));
        MzIndex index = MzIndex.ofPrecursors(library);

        assertEquals(List.of(library.get(0), library.get(2)), index.find(200.0, 0.01, spectrum -> Math.abs(spectrum.getPrecursorMz() - 200.0) < 0.01));
    }

    @Test
    void emptyLibraryHasNoMatches() {
        assertTrue(MzIndex.ofPeaks(List.of()).find(100.0, 0.01, spectrum -> true).isEmpty());
    }
}
