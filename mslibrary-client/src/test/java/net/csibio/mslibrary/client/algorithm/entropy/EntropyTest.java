package net.csibio.mslibrary.client.algorithm.entropy;

import net.csibio.aird.bean.common.Spectrum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntropyTest {

    @Test
    void singleValueArrayHasZeroEntropy() {
        assertEquals(0.0, Entropy.getEntropy(new double[]{42.0}), 1e-9);
    }

    @Test
    void uniformDistributionHasMaximumEntropy() {
        // four equal-intensity peaks: entropy should equal ln(4), the theoretical max for n=4
        double entropy = Entropy.getEntropy(new double[]{1, 1, 1, 1});
        assertEquals(Math.log(4), entropy, 1e-9);
    }

    @Test
    void normalizedEntropyOfUniformDistributionIsOne() {
        assertEquals(1.0, Entropy.getNormalizedEntropy(new double[]{1, 1, 1, 1}), 1e-9);
    }

    @Test
    void skewedDistributionHasLowerEntropyThanUniformDistribution() {
        double skewed = Entropy.getEntropy(new double[]{100, 1, 1, 1});
        double uniform = Entropy.getEntropy(new double[]{1, 1, 1, 1});
        assertTrue(skewed < uniform);
    }

    @Test
    void spectrumEntropyDelegatesToIntensityArray() {
        Spectrum spectrum = new Spectrum(new double[]{100, 200, 300, 400}, new double[]{1, 1, 1, 1});
        assertEquals(Math.log(4), Entropy.getSpectrumEntropy(spectrum), 1e-9);
    }
}
