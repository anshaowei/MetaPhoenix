package net.csibio.mslibrary.client.utils;

import java.util.List;

public class ArrayUtil {

    public static double sum(double[] array) {
        double sum = 0;
        for (double d : array) {
            sum += d;
        }
        return sum;
    }

    public static double[] toArray(List<Double> list) {
        double[] target = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            target[i] = list.get(i);
        }
        return target;
    }


    public static double[] normalize(double[] array, double value) {
        if (value > 0) {
            for (int i = 0; i < array.length; i++) {
                array[i] /= value;
            }
        }
        return array;
    }

    public static int findNearestIndex(double[] mzs, double mz) {
        int index = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < mzs.length; i++) {
            double diff = Math.abs(mzs[i] - mz);
            if (diff < min) {
                min = diff;
                index = i;
            }
        }
        return index;
    }

    public static double findNearestDiff(double[] mzs, double mz) {
        return Math.abs(mzs[findNearestIndex(mzs, mz)] - mz);
    }
}
