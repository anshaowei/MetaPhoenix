package net.csibio.mslibrary.client.algorithm.entropy;

import net.csibio.aird.bean.common.Spectrum;
import org.springframework.stereotype.Component;

@Component("entropy")
public class Entropy {

    public double getEntropy(double[] intensityArray) {
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

    /**
     * 用以计算熵的谱图必须是归一化的，使得谱图的信号和为1
     *
     * @param spectrum
     * @return
     */
    public double getEntropy(Spectrum spectrum) {
        return getEntropy(spectrum.getInts());
    }

}

