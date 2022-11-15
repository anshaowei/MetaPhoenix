package net.csibio.mslibrary.client.algorithm.entropy;

import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import org.springframework.stereotype.Component;

@Component("entropy")
public class Entropy {

    /**
     * 谱图需要经过归一化，使得每张谱图信号的和为1
     *
     * @param intensityArray
     * @return
     */
    public Double getEntropy(double[] intensityArray) {
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

    public Double getEntropy(Spectrum spectrum) {
        return getEntropy(spectrum.getInts());
    }

    public Double getEntropy(SpectrumDO spectrumDO) {
        return getEntropy(spectrumDO.getInts());
    }

}

