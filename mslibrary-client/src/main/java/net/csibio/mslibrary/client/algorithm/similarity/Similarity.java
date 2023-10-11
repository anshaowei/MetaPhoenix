package net.csibio.mslibrary.client.algorithm.similarity;

import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.algorithm.entropy.Entropy;
import net.csibio.mslibrary.client.constants.enums.SpectrumMatchMethod;
import net.csibio.mslibrary.client.utils.SpectrumUtil;
import org.apache.commons.math3.stat.StatUtils;

public class Similarity {

    public static double getScore(Spectrum querySpectrum, Spectrum libSpectrum, SpectrumMatchMethod spectrumMatchMethod, double mzTolerance) {
        double score = 0;
        mzTolerance = 0.05;
        Spectrum spectrumA = SpectrumUtil.clone(querySpectrum);
        Spectrum spectrumB = SpectrumUtil.clone(libSpectrum);
        SpectrumUtil.normalize(spectrumA);
        SpectrumUtil.normalize(spectrumB);
        switch (spectrumMatchMethod) {
            case Cosine -> score = getCosineSimilarity(spectrumA, spectrumB, mzTolerance, false);
            case Cosine_SquareRoot -> score = getSRCosineSimilarity(spectrumA, spectrumB, mzTolerance, false);
            case Entropy -> score = getEntropySimilarity(spectrumA, spectrumB, mzTolerance);
            case Unweighted_Entropy -> score = getUnWeightedEntropySimilarity(spectrumA, spectrumB, mzTolerance);
            case MetaPro -> score = getMetaProSimilarity(spectrumA, spectrumB, mzTolerance);
            case Weighted_Cosine -> score = getCosineSimilarity(spectrumA, spectrumB, mzTolerance, true);
            default -> {
            }
        }
        if (score < 0) {
            score = 0.0;
        }
        if (score > 1) {
            score = 1.0;
        }
        return score;
    }

    private static double getMetaProSimilarity(Spectrum querySpectrum, Spectrum libSpectrum, double mzTolerance) {
        int expIndex = 0;
        double[] libMzArray = libSpectrum.getMzs();
        double[] libIntArray = libSpectrum.getInts();
        double[] expMzArray = querySpectrum.getMzs();
        double[] expIntArray = querySpectrum.getInts();

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

    private static double getUnWeightedEntropySimilarity(Spectrum querySpectrum, Spectrum libSpectrum, double mzTolerance) {
        double entropyA = Entropy.getSpectrumEntropy(querySpectrum);
        double entropyB = Entropy.getSpectrumEntropy(libSpectrum);

        Spectrum mixSpectrum = SpectrumUtil.mixByWeight(querySpectrum, libSpectrum, 1, 1, mzTolerance);
        SpectrumUtil.normalize(mixSpectrum);
        double entropyMix = Entropy.getSpectrumEntropy(mixSpectrum);

        return 1 - (2 * entropyMix - entropyA - entropyB) / Math.log(4);
    }

    private static double getEntropySimilarity(Spectrum querySpectrum, Spectrum libSpectrum, double mzTolerance) {
        double entropyA = Entropy.getSpectrumEntropy(querySpectrum);
        double entropyB = Entropy.getSpectrumEntropy(libSpectrum);
        double weightA;
        double weightB;

        //dynamic weight
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

        //mix spectrum
        Spectrum mixSpectrum = SpectrumUtil.mixByWeight(querySpectrum, libSpectrum, weightA, weightB, mzTolerance);
        SpectrumUtil.normalize(mixSpectrum);
        double entropyMix = Entropy.getSpectrumEntropy(mixSpectrum);

        return 1 - (2 * entropyMix - entropyA - entropyB) / Math.log(4);
    }

    private static double getCosineSimilarity(Spectrum querySpectrum, Spectrum libSpectrum, double mzTolerance, boolean isWeighted) {
        double[] libMzArray = libSpectrum.getMzs();
        double[] libIntArray = libSpectrum.getInts();
        double[] expMzArray = querySpectrum.getMzs();
        double[] expIntArray = querySpectrum.getInts();

        int expIndex = 0, libIndex = 0, matchCount = 0;
        double dotProduct = 0d, expNorm = 0d, libNorm = 0d;
        while (expIndex < expMzArray.length && libIndex < libMzArray.length) {
            if (expMzArray[expIndex] < libMzArray[libIndex] - mzTolerance) {
                if (isWeighted) {
                    expNorm += weightedDotProduct(expMzArray[expIndex], expIntArray[expIndex]) * weightedDotProduct(expMzArray[expIndex], expIntArray[expIndex]);
                } else {
                    expNorm += expIntArray[expIndex] * expIntArray[expIndex];
                }
                expIndex++;
            } else if (expMzArray[expIndex] > libMzArray[libIndex] + mzTolerance) {
                if (isWeighted) {
                    libNorm += weightedDotProduct(libMzArray[libIndex], libIntArray[libIndex]) * weightedDotProduct(libMzArray[libIndex], libIntArray[libIndex]);
                } else {
                    libNorm += libIntArray[libIndex] * libIntArray[libIndex];
                }
                libIndex++;
            } else {
                if (isWeighted) {
                    dotProduct += weightedDotProduct(expMzArray[expIndex], expIntArray[expIndex]) * weightedDotProduct(libMzArray[libIndex], libIntArray[libIndex]);
                    expNorm += weightedDotProduct(expMzArray[expIndex], expIntArray[expIndex]) * weightedDotProduct(expMzArray[expIndex], expIntArray[expIndex]);
                    libNorm += weightedDotProduct(libMzArray[libIndex], libIntArray[libIndex]) * weightedDotProduct(libMzArray[libIndex], libIntArray[libIndex]);
                } else {
                    dotProduct += expIntArray[expIndex] * libIntArray[libIndex];
                    expNorm += expIntArray[expIndex] * expIntArray[expIndex];
                    libNorm += libIntArray[libIndex] * libIntArray[libIndex];
                }
                matchCount++;
                expIndex++;
                libIndex++;
            }
        }
        while (expIndex < expMzArray.length) {
            if (isWeighted) {
                expNorm += weightedDotProduct(expMzArray[expIndex], expIntArray[expIndex]) * weightedDotProduct(expMzArray[expIndex], expIntArray[expIndex]);
            } else {
                expNorm += expIntArray[expIndex] * expIntArray[expIndex];
            }
            expIndex++;
        }
        while (libIndex < libMzArray.length) {
            if (isWeighted) {
                libNorm += weightedDotProduct(libMzArray[libIndex], libIntArray[libIndex]) * weightedDotProduct(libMzArray[libIndex], libIntArray[libIndex]);
            } else {
                libNorm += libIntArray[libIndex] * libIntArray[libIndex];
            }
            libIndex++;
        }
        if (expNorm == 0 || libNorm == 0) {
            return 0;
        }
        return dotProduct * dotProduct / expNorm / libNorm;
    }

    private static double getSRCosineSimilarity(Spectrum querySpectrum, Spectrum libSpectrum, double mzTolerance, boolean isWeighted) {
        return Math.sqrt(getCosineSimilarity(querySpectrum, libSpectrum, mzTolerance, isWeighted));
    }

    private static double weightedDotProduct(double mz, double intensity) {
        return Math.pow(mz, 3) * Math.pow(intensity, 0.6);
    }

}