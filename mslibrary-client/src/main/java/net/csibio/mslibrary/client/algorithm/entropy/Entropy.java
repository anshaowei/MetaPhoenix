package net.csibio.mslibrary.client.algorithm.entropy;

import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
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
     * 计算谱图熵的谱图必须经过归一化，使得每张谱图的信号和为1
     *
     * @param spectrum
     * @return
     */
    public double getEntropy(Spectrum spectrum) {
        return getEntropy(spectrum.getInts());
    }

    /**
     * 计算谱图熵的谱图必须经过归一化，使得每张谱图的信号和为1
     *
     * @param spectrumDO
     * @return
     */
    public double getEntropy(SpectrumDO spectrumDO) {
        return getEntropy(spectrumDO.getInts());
    }

}

