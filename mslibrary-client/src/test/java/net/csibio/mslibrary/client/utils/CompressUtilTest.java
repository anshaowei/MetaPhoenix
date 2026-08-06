package net.csibio.mslibrary.client.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CompressUtilTest {

    @Test
    void encodeThenDecodeRoundTripsToTheOriginalArray() {
        double[] original = {100.123, 200.456, 300.789, 0.0, -55.4};
        byte[] encoded = CompressUtil.encode(original);
        double[] decoded = CompressUtil.decode(encoded);
        assertArrayEquals(original, decoded, 1e-9);
    }

    @Test
    void encodeOfNullReturnsNull() {
        assertNull(CompressUtil.encode(null));
    }

    @Test
    void decodeOfNullReturnsNull() {
        assertNull(CompressUtil.decode(null));
    }

    @Test
    void roundTripPreservesEmptyArray() {
        double[] original = {};
        byte[] encoded = CompressUtil.encode(original);
        double[] decoded = CompressUtil.decode(encoded);
        assertArrayEquals(original, decoded, 1e-9);
    }
}
