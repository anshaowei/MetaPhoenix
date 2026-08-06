package net.csibio.mslibrary.client.algorithm.similarity;

import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.constants.enums.SpectrumMatchMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimilarityTest {

    private static Spectrum spectrum(double[] mzs, double[] ints) {
        return new Spectrum(mzs, ints);
    }

    @Test
    void identicalSpectraScoreOneOnCosine() {
        Spectrum a = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Spectrum b = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        assertEquals(1.0, Similarity.getScore(a, b, SpectrumMatchMethod.Cosine, 0.05), 1e-9);
    }

    @Test
    void identicalSpectraScoreOneOnCosineSquareRoot() {
        Spectrum a = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Spectrum b = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        assertEquals(1.0, Similarity.getScore(a, b, SpectrumMatchMethod.Cosine_SquareRoot, 0.05), 1e-9);
    }

    @Test
    void identicalSpectraScoreOneOnWeightedCosine() {
        Spectrum a = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Spectrum b = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        assertEquals(1.0, Similarity.getScore(a, b, SpectrumMatchMethod.Weighted_Cosine, 0.05), 1e-9);
    }

    @Test
    void identicalSpectraScoreOneOnEntropy() {
        Spectrum a = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Spectrum b = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        assertEquals(1.0, Similarity.getScore(a, b, SpectrumMatchMethod.Entropy, 0.05), 1e-9);
    }

    @Test
    void identicalSpectraScoreOneOnUnweightedEntropy() {
        Spectrum a = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Spectrum b = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        assertEquals(1.0, Similarity.getScore(a, b, SpectrumMatchMethod.Unweighted_Entropy, 0.05), 1e-9);
    }

    @Test
    void identicalSpectraScoreOneOnMetaPro() {
        Spectrum a = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Spectrum b = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        assertEquals(1.0, Similarity.getScore(a, b, SpectrumMatchMethod.MetaPro, 0.05), 1e-9);
    }

    @Test
    void completelyDisjointSpectraScoreZeroOnCosine() {
        Spectrum a = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Spectrum b = spectrum(new double[]{1000, 2000, 3000}, new double[]{5, 15, 25});
        assertEquals(0.0, Similarity.getScore(a, b, SpectrumMatchMethod.Cosine, 0.05), 1e-9);
    }

    @Test
    void completelyDisjointSpectraScoreZeroOnCosineSquareRoot() {
        Spectrum a = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Spectrum b = spectrum(new double[]{1000, 2000, 3000}, new double[]{5, 15, 25});
        assertEquals(0.0, Similarity.getScore(a, b, SpectrumMatchMethod.Cosine_SquareRoot, 0.05), 1e-9);
    }

    @Test
    void completelyDisjointSpectraScoreZeroOnMetaPro() {
        Spectrum a = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Spectrum b = spectrum(new double[]{1000, 2000, 3000}, new double[]{5, 15, 25});
        assertEquals(0.0, Similarity.getScore(a, b, SpectrumMatchMethod.MetaPro, 0.05), 1e-9);
    }

    @Test
    void completelyDisjointSpectraScoreZeroOnWeightedCosine() {
        Spectrum a = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Spectrum b = spectrum(new double[]{1000, 2000, 3000}, new double[]{5, 15, 25});
        assertEquals(0.0, Similarity.getScore(a, b, SpectrumMatchMethod.Weighted_Cosine, 0.05), 1e-9);
    }

    @Test
    void scoreIsAlwaysClampedBetweenZeroAndOne() {
        Spectrum a = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Spectrum b = spectrum(new double[]{150, 250, 305}, new double[]{8, 18, 33});
        for (SpectrumMatchMethod method : SpectrumMatchMethod.values()) {
            double score = Similarity.getScore(a, b, method, 0.05);
            assertTrue(score >= 0.0 && score <= 1.0, method + " produced out-of-range score " + score);
        }
    }

    @Test
    void getScoreDoesNotMutateInputSpectra() {
        Spectrum a = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Spectrum b = spectrum(new double[]{100, 200, 300}, new double[]{10, 20, 30});
        Similarity.getScore(a, b, SpectrumMatchMethod.Entropy, 0.05);
        assertEquals(10.0, a.getInts()[0], 1e-9);
        assertEquals(30.0, b.getInts()[2], 1e-9);
    }
}
