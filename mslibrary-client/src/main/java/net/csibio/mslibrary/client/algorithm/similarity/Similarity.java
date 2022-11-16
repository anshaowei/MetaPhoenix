package net.csibio.mslibrary.client.algorithm.similarity;

import net.csibio.aird.bean.common.Spectrum;
import org.apache.commons.math3.stat.StatUtils;
import org.springframework.stereotype.Component;

@Component("similarity")
public class Similarity {

    public double getDotProduct(Spectrum runSpectrum, Spectrum libSpectrum, double mzTolerance) {
        int expIndex = 0;
        double[] libMzArray = libSpectrum.getMzs();
        double[] libIntArray = libSpectrum.getInts();
        double[] expMzArray = runSpectrum.getMzs();
        double[] expIntArray = runSpectrum.getInts();

        //librarySpectrum的最大值
        double maxLibIntensity = StatUtils.max(libIntArray);
        double maxExpIntensity = StatUtils.max(expIntArray);

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


}
