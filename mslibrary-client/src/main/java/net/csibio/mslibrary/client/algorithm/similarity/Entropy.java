package net.csibio.mslibrary.client.algorithm.similarity;

import net.csibio.aird.bean.common.Spectrum;

public class Entropy {

    public static double getEntropy(double[] array) {
        double entropy = 0;
        for (double d : array) {
            if (d > 0) {
                entropy += d * Math.log(d);
            }
        }
        return -entropy;
    }

    public static double getEntropy(Spectrum spectrum) {
        return getEntropy(spectrum.getInts());
    }

}
