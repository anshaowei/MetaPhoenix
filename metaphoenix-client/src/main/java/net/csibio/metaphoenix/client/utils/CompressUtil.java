package net.csibio.metaphoenix.client.utils;

import net.csibio.aird.compressor.bytecomp.ByteComp;
import net.csibio.aird.compressor.bytecomp.ZstdWrapper;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

public class CompressUtil {

    public static byte[] encode(double[] target) {
        if (target == null) {
            return null;
        }
        DoubleBuffer doubleBuffer = DoubleBuffer.wrap(target);
        ByteBuffer bbTarget = ByteBuffer.allocate(doubleBuffer.capacity() * 8);
        bbTarget.asDoubleBuffer().put(doubleBuffer);
        byte[] targetArray = bbTarget.array();
        byte[] compressedArray = new ZstdWrapper().encode(targetArray);
        return compressedArray;
    }

    public static double[] decode(byte[] target) {
        if (target == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(target);
        byteBuffer = ByteBuffer.wrap(new ZstdWrapper().decode(byteBuffer.array()));

        DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
        double[] doubleValues = new double[doubleBuffer.capacity()];
        for (int i = 0; i < doubleBuffer.capacity(); i++) {
            doubleValues[i] = doubleBuffer.get(i);
        }

        byteBuffer.clear();
        return doubleValues;
    }

    public static byte[] encode(double[] target, ByteComp byteComp) {
        if (target == null) {
            return null;
        }
        DoubleBuffer doubleBuffer = DoubleBuffer.wrap(target);
        ByteBuffer bbTarget = ByteBuffer.allocate(doubleBuffer.capacity() * 8);
        bbTarget.asDoubleBuffer().put(doubleBuffer);
        byte[] targetArray = bbTarget.array();
        byte[] compressedArray = byteComp.encode(targetArray);
        return compressedArray;
    }

    public static double[] decode(byte[] target, ByteComp byteComp) {
        if (target == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(target);
        byteBuffer = ByteBuffer.wrap(byteComp.decode(byteBuffer.array()));

        DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
        double[] doubleValues = new double[doubleBuffer.capacity()];
        for (int i = 0; i < doubleBuffer.capacity(); i++) {
            doubleValues[i] = doubleBuffer.get(i);
        }

        byteBuffer.clear();
        return doubleValues;
    }
}
