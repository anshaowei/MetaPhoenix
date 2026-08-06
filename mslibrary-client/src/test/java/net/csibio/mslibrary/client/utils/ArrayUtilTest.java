package net.csibio.mslibrary.client.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArrayUtilTest {

    @Test
    void sumAddsAllElements() {
        assertEquals(6.0, ArrayUtil.sum(new double[]{1, 2, 3}), 1e-9);
    }

    @Test
    void sumOfEmptyArrayIsZero() {
        assertEquals(0.0, ArrayUtil.sum(new double[]{}), 1e-9);
    }

    @Test
    void averageDividesSumByLength() {
        assertEquals(2.0, ArrayUtil.average(new double[]{1, 2, 3}), 1e-9);
    }

    @Test
    void toArrayConvertsListPreservingOrder() {
        double[] result = ArrayUtil.toArray(List.of(1.5, 2.5, 3.5));
        assertArrayEquals(new double[]{1.5, 2.5, 3.5}, result, 1e-9);
    }

    @Test
    void normalizeDividesEachElementByGivenValue() {
        double[] array = {2, 4, 8};
        double[] result = ArrayUtil.normalize(array, 2);
        assertArrayEquals(new double[]{1, 2, 4}, result, 1e-9);
    }

    @Test
    void normalizeWithNonPositiveValueLeavesArrayUnchanged() {
        double[] array = {2, 4, 8};
        double[] result = ArrayUtil.normalize(array, 0);
        assertArrayEquals(new double[]{2, 4, 8}, result, 1e-9);
    }

    @Test
    void findNearestIndexReturnsIndexOfClosestValue() {
        double[] mzs = {100.0, 200.0, 300.0, 400.0, 500.0};
        assertEquals(2, ArrayUtil.findNearestIndex(mzs, 305.0));
        assertEquals(0, ArrayUtil.findNearestIndex(mzs, 95.0));
        assertEquals(4, ArrayUtil.findNearestIndex(mzs, 999.0));
    }

    @Test
    void findNearestDiffReturnsAbsoluteDistanceToClosestValue() {
        double[] mzs = {100.0, 200.0, 300.0};
        assertEquals(5.0, ArrayUtil.findNearestDiff(mzs, 305.0), 1e-9);
    }
}
