package net.csibio.mslibrary.client.algorithm.score;

import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("spectrumScorer")
public class SpectrumScorer {

    @Autowired
    IsotopeCalculator isotopeCalculator;
    @Autowired
    IsotopeFinder isotopeFinder;

    public double ms1ForwardScore(Spectrum ms1Spectrum, Spectrum libSpectrum, double mzTolerance) {
        return forwardSimilarity(ms1Spectrum, libSpectrum, mzTolerance);
    }

    public double ms1ReverseScore(Spectrum ms1Spectrum, Spectrum libSpectrum, double mzTolerance) {
        return forwardSimilarity(libSpectrum, ms1Spectrum, mzTolerance);
    }

    public double ms2ForwardScore(Spectrum ms2Spectrum, Spectrum libSpectrum, double mzTolerance) {
        return forwardSimilarity(ms2Spectrum, libSpectrum, mzTolerance);
    }

    public double ms2ReverseScore(Spectrum ms2Spectrum, Spectrum libSpectrum, double mzTolerance) {
        return forwardSimilarity(libSpectrum, ms2Spectrum, mzTolerance);
    }

    public double ms1IsotopeScore(Spectrum ms1Spectrum, String formula, double monoMz, double mzTolerance, boolean isPpm) {
        int maxIsotope = 4;
        Double[] theoDistribution = isotopeCalculator.getTheoDistribution(formula, maxIsotope);
        if (theoDistribution == null || theoDistribution.length < 2) {
            return 0;
        }
        maxIsotope = Math.min(maxIsotope, theoDistribution.length);
        Double[] runDistribution = isotopeFinder.getRunDistribution(ms1Spectrum, monoMz, mzTolerance, isPpm, maxIsotope);

        return getIsotopeIntensitySimilarity(runDistribution, theoDistribution);
    }

    public double ms1AdductScore(Spectrum ms1Spectrum, List<Double> adductMzs, List<Double> libIntensities, double tolerance) {
        double[] mzArray = ms1Spectrum.getMzs();
        double[] intensityArray = ms1Spectrum.getInts();
        int adductIndex = 0;
        List<Double> expIntensities = new ArrayList<>();
        double intensity = 0;
        for (int i = 0; i < mzArray.length; i++) {
            if (mzArray[i] < adductMzs.get(adductIndex) - tolerance) {
                continue;
            }
            if (adductMzs.get(adductIndex) - tolerance <= mzArray[i] && mzArray[i] <= adductMzs.get(adductIndex) + tolerance) {
                intensity += intensityArray[i];
            } else {
                expIntensities.add(intensity);
                intensity = 0;
                adductIndex++;
                i--;
            }
        }
        return getAdductIntensitySimilarity(expIntensities, libIntensities);
    }

    //返回库的命中比例
    private double forwardSimilarity(Spectrum runSpectrum, Spectrum libSpectrum, double mzTolerance) {
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

    private double getIsotopeIntensitySimilarity(Double[] expDistribution, Double[] theoDistribution) {
        double expRatio, theoRatio, error = 0;
        for (int i = 1; i < Math.min(expDistribution.length, theoDistribution.length); i++) {
            if (expDistribution[i] == null) {
                break;
            }
            expRatio = expDistribution[i] / expDistribution[0];
            theoRatio = theoDistribution[i] / theoDistribution[0];
            if (expRatio <= 1 && theoRatio <= 1) {
                error += Math.abs(expRatio - theoRatio);
            } else {
                if (expRatio > theoRatio) {
                    error += 1 - theoRatio / expRatio;
                } else if (theoRatio > expRatio) {
                    error += 1 - expRatio / theoRatio;
                }
            }
        }
        return 1 - error;
    }

    private double getAdductIntensitySimilarity(List<Double> expIntensities, List<Double> libIntensities) {
        double experimentSum = 0.0d, librarySum = 0.0d;
        for (int i = 0; i < libIntensities.size(); i++) {
            experimentSum += expIntensities.get(i); //sum of experiment
            librarySum += libIntensities.get(i); //sum of library
        }
        double[] expSqrt = new double[expIntensities.size()];
        double[] libSqrt = new double[libIntensities.size()];
        for (int i = 0; i < expSqrt.length; i++) {
            expSqrt[i] = FastMath.sqrt(expIntensities.get(i));
            libSqrt[i] = FastMath.sqrt(libIntensities.get(i));
        }
        double expVecNorm = FastMath.sqrt(experimentSum);
        double libVecNorm = FastMath.sqrt(librarySum);

        double[] expSqrtVecNormed = ArrayUtil.normalize(expSqrt, expVecNorm);
        double[] libSqrtVecNormed = ArrayUtil.normalize(libSqrt, libVecNorm);

        double sumOfMult = 0d;
        for (int i = 0; i < expSqrt.length; i++) {
            sumOfMult += expSqrtVecNormed[i] * libSqrtVecNormed[i];
        }
        return sumOfMult;
    }
}
