package net.csibio.mslibrary.client.algorithm.score;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsotopeCalculatorTest {

    private final IsotopeCalculator calculator = new IsotopeCalculator();

    @Test
    void nullFormulaReturnsNull() {
        assertNull(calculator.getTheoDistribution(null, 5));
    }

    @Test
    void formulaWithSingleElementReturnsNull() {
        // the implementation only computes a distribution when at least two distinct elements are present
        assertNull(calculator.getTheoDistribution("C6", 5));
    }

    @Test
    void distributionProbabilitiesSumToApproximatelyOne() {
        // glucose-like formula C6H12O6: the theoretical isotope envelope should be a valid probability distribution
        Double[] distribution = calculator.getTheoDistribution("C6H12O6", 0);
        double sum = 0;
        for (Double d : distribution) {
            sum += d;
        }
        assertTrue(sum > 0.95 && sum < 1.05, "isotope distribution should sum to ~1, got " + sum);
    }

    @Test
    void monoisotopicPeakIsTheLargestForASmallOrganicFormula() {
        Double[] distribution = calculator.getTheoDistribution("C6H12O6", 0);
        double monoIntensity = distribution[0];
        for (Double d : distribution) {
            assertTrue(d <= monoIntensity, "expected monoisotopic (M0) peak to be the most abundant");
        }
    }

    @Test
    void maxIsotopeCapsTheLengthOfTheReturnedDistribution() {
        Double[] distribution = calculator.getTheoDistribution("C6H12O6", 3);
        assertEquals(3, distribution.length);
    }
}
