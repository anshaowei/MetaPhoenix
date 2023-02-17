package net.csibio.mslibrary.client.algorithm.score;

import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.algorithm.similarity.Similarity;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.checkerframework.checker.units.qual.A;
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
        return Similarity.getDotProduct(ms1Spectrum, libSpectrum, mzTolerance);
    }

    public double ms1ReverseScore(Spectrum ms1Spectrum, Spectrum libSpectrum, double mzTolerance) {
        return Similarity.getDotProduct(libSpectrum, ms1Spectrum, mzTolerance);
    }

    public double ms2ForwardScore(Spectrum ms2Spectrum, Spectrum libSpectrum, double mzTolerance) {
        return Similarity.getDotProduct(ms2Spectrum, libSpectrum, mzTolerance);
    }

    public double ms2ReverseScore(Spectrum ms2Spectrum, Spectrum libSpectrum, double mzTolerance) {
        return Similarity.getDotProduct(libSpectrum, ms2Spectrum, mzTolerance);
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
