package net.csibio.mslibrary.client.algorithm.similarity;

import net.csibio.aird.bean.common.Spectrum;

public class Entropy {

    public static double getEntropy(double[] intensityArray) {
        double sum = 0;
        for (double intensity : intensityArray) {
            sum += intensity;
        }
        double entropy = 0;
        for (double intensity : intensityArray) {
            double p = intensity / sum;
            entropy += p * Math.log(p);
        }
        return -entropy;
    }

    public static double getEntropy(Spectrum spectrum) {
        return getEntropy(spectrum.getInts());
    }

}
