package net.csibio.metaphoenix.client.algorithm.entropy;

import net.csibio.aird.bean.common.Spectrum;
import net.csibio.metaphoenix.client.utils.ArrayUtil;

public class Entropy {

    /**
     * Shannon entropy of the values normalized to sum 1. Zero values contribute nothing (0 * ln 0 = 0),
     * and empty, single-value or all-zero input has entropy 0.
     */
    public static double getEntropy(double[] values) {
        if (values.length <= 1) {
            return 0;
        }
        double sum = ArrayUtil.sum(values);
        if (sum <= 0) {
            return 0;
        }
        double entropy = 0;
        boolean isMax = true;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != values[0]) {
                isMax = false;
            }
            if (values[i] == 0) {
                continue;
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
