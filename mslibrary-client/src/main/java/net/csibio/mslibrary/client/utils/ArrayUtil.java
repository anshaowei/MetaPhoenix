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

    public static double average(double[] array) {
        return sum(array) / array.length;
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
        int left = 0, right = 0;
        for (right = mzs.length - 1; left != right; ) {
            int midIndex = (right + left) / 2;
            int mid = (right - left);
            double midValue = mzs[midIndex];
            if (mz == midValue) {
                return midIndex;
            }
            if (mz > midValue) {
                left = midIndex;
            } else {
                right = midIndex;
            }
            if (mid <= 2) {
                break;
            }
        }
        return (Math.abs(mzs[right] - mz) > Math.abs(mzs[left] - mz) ? left : right);
    }

    public static double findNearestDiff(double[] mzs, double mz) {
        return Math.abs(mzs[findNearestIndex(mzs, mz)] - mz);
    }
}
