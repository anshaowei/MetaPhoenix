package net.csibio.mslibrary.client.algorithm.similarity;

import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.algorithm.entropy.Entropy;
import net.csibio.mslibrary.client.utils.SpectrumUtil;
import org.apache.commons.math3.stat.StatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("similarity")
public class Similarity {

    @Autowired
    Entropy entropy;

    public double getDotProduct(Spectrum runSpectrum, Spectrum libSpectrum, double mzTolerance) {
        int expIndex = 0;
        double[] libMzArray = libSpectrum.getMzs();
        double[] libIntArray = libSpectrum.getInts();
        double[] expMzArray = runSpectrum.getMzs();
        double[] expIntArray = runSpectrum.getInts();

        //librarySpectrum的最大值
        double maxLibIntensity = StatUtils.max(libIntArray);

        int libCounter = 0, expCounter = 0;
        double dotProduct = 0d, libNorm = 0d, expNorm = 0d;
        for (int libIndex = 0; libIndex < libMzArray.length; libIndex++) {
            double leftMz = libMzArray[libIndex] - mzTolerance;
            double rightMz = libMzArray[libIndex] + mzTolerance;

            //统计lib中大于最大值百分之一的部分
            double libIntensity = libIntArray[libIndex];
            if (libIntensity < 0.01 * maxLibIntensity) {
                continue;
            }
            int libBinWidth = 1;
            while (libIndex + libBinWidth < libMzArray.length && libMzArray[libIndex + libBinWidth] < rightMz) {
                libIntensity += libIntArray[libIndex + libBinWidth];
                libBinWidth++;
            }
            libIndex += libBinWidth - 1;
            libCounter++;
            //统计exp中和lib相对应的部分
            double expIntensity = 0;
            for (; expIndex < expMzArray.length; expIndex++) {
                if (expMzArray[expIndex] < leftMz) {
                    continue;
                }
                if (leftMz <= expMzArray[expIndex] && expMzArray[expIndex] < rightMz) {
                    expIntensity += expIntArray[expIndex];
                } else {
                    break;
                }
            }
            libNorm += libIntensity * libIntensity;
            if (expIntensity > 0) {
                expCounter++;
            }
            expNorm += expIntensity * expIntensity;
            dotProduct += expIntensity * libIntensity;
        }
        if (libNorm == 0 || expNorm == 0 || libCounter == 0) {
            return 0;
        }
        return dotProduct / Math.sqrt(libNorm) / Math.sqrt(expNorm) * expCounter / libCounter;
    }

    public double getUnWeightedEntropySimilarity(Spectrum spectrum1, Spectrum spectrum2) {

        Spectrum spectrumA = SpectrumUtil.clone(spectrum1);
        Spectrum spectrumB = SpectrumUtil.clone(spectrum2);
        SpectrumUtil.normalize(spectrumA);
        SpectrumUtil.normalize(spectrumB);

        Spectrum mixSpectrum = SpectrumUtil.mix(spectrumA, spectrumB);

        double entropyA = entropy.getEntropy(spectrumA);
        double entropyB = entropy.getEntropy(spectrumB);
        double entropyMix = entropy.getEntropy(mixSpectrum);

        return 1 - (entropyMix - 0.5 * (entropyA + entropyB)) / Math.log(2);
    }

    public double getEntropySimilarity(Spectrum spectrum1, Spectrum spectrum2) {
        Spectrum spectrumA = SpectrumUtil.clone(spectrum1);
        Spectrum spectrumB = SpectrumUtil.clone(spectrum2);
        SpectrumUtil.normalize(spectrumA);
        SpectrumUtil.normalize(spectrumB);

        double entropyA = entropy.getEntropy(spectrumA);
        double entropyB = entropy.getEntropy(spectrumB);
        double weightA;
        double weightB;

        //给出两个谱图的权重，取值范围0~1
        if (entropyA >= 3) {
            weightA = 1;
        } else {
            weightA = 0.25 + entropyA * 0.25;
        }
        if (entropyB >= 3) {
            weightB = 1;
        } else {
            weightB = 0.25 + entropyB * 0.25;
        }

        //根据权重混合两张谱图
        Spectrum mixSpectrum = SpectrumUtil.mixByWeight(spectrumA, spectrumB, weightA, weightB);
        double entropyMix = entropy.getEntropy(mixSpectrum);

        return 1 - (entropyMix - 0.5 * (entropyA + entropyB)) / Math.log(2);
    }

}