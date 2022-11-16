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
}
