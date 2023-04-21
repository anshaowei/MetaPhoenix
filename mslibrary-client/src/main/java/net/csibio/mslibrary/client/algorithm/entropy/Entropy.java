package net.csibio.mslibrary.client.algorithm.entropy;

import net.csibio.aird.bean.common.Spectrum;

public class Entropy {

    public static double getEntropy(double[] values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        double entropy = 0;
        for (double value : values) {
            double p = value / sum;
            entropy += p * Math.log(p);
        }
        return -entropy;
    }

    public static double getSpectrumEntropy(Spectrum spectrum) {
        return getEntropy(spectrum.getInts());
    }

}
