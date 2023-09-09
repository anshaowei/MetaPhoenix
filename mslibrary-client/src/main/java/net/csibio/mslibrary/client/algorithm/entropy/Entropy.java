package net.csibio.mslibrary.client.algorithm.entropy;

import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.utils.ArrayUtil;

public class Entropy {

    public static double getEntropy(double[] values) {
        if (values.length == 1) {
            return 0;
        }
        double sum = ArrayUtil.sum(values);
        double entropy = 0;
        boolean isMax = true;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != values[0]) {
                isMax = false;
            }
            double p = values[i] / sum;
            entropy += p * Math.log(p);
        }
        if (isMax) {
            return Math.log(values.length);
        }
        return -entropy;
    }

    public static double getNormalizedEntropy(double[] values) {
        return (values.length == 1) ? 0 : getEntropy(values) / Math.log(values.length);
    }

    public static double getSpectrumEntropy(Spectrum spectrum) {
        return getEntropy(spectrum.getInts());
    }

}
