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

    public static int binarySearch(double[] mzs, double mz){
        if (mzs.length > 0) {
            int low = 0;
            int high = mzs.length - 1;
            while (low <= high) {
                int mid = (low + high) >>> 1;
                int c = Double.compare(mzs[mid], mz);
                if (c < 0)
                    low = mid + 1;
                else if (c > 0)
                    high = mid - 1;
                else
                    return mid; // key found
            }
            return -(low + 1);
        }
        return -1;
    }
}
