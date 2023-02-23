package net.csibio.mslibrary.client.utils;

import net.csibio.aird.bean.common.Spectrum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpectrumUtil {

    /**
     * normalize the spectrum
     */
    public static void normalize(Spectrum spectrum) {
        double sum = ArrayUtil.sum(spectrum.getInts());
        ArrayUtil.normalize(spectrum.getInts(), sum);
    }

    /**
     * mix two spectrum by the given weight
     */
    public static Spectrum mixByWeight(Spectrum spectrum1, Spectrum spectrum2, double weight1, double weight2, double mzTolerance) {

        HashMap<Double, Double> mixMap = new HashMap<>();
        HashMap<Double, Double> spectrum2Map = new HashMap<>();
        List<Double> spectrum2MzList = new ArrayList<>();
        for (int i = 0; i < spectrum2.getMzs().length; i++) {
            spectrum2MzList.add(spectrum2.getMzs()[i]);
            spectrum2Map.put(spectrum2.getMzs()[i], spectrum2.getInts()[i]);
        }

        //match mz in the two spectra
        for (int i = 0; i < spectrum1.getMzs().length; i++) {
            double mz1 = spectrum1.getMzs()[i];
            List<Double> candidateMzList = spectrum2MzList.stream().filter(mz2 -> Math.abs(mz1 - mz2) <= mzTolerance).toList();
            if (candidateMzList.size() > 0) {
                double diff = Double.MAX_VALUE;
                double mz2 = 0;
                for (double candidate : candidateMzList) {
                    double tempDiff = Math.abs(mz1 - candidate);
                    if (tempDiff < diff) {
                        mz2 = candidate;
                        diff = tempDiff;
                    }
                }
                double mixMz = (mz1 + mz2) / 2;
                double mixIntensity = spectrum1.getInts()[i] * weight1 + spectrum2Map.get(mz2) * weight2;
                mixMap.put(mixMz, mixIntensity);
                spectrum2MzList.remove(mz2);
            } else {
                mixMap.put(mz1, spectrum1.getInts()[i]);
            }
        }

        //add the rest of mz2List
        for (double mz2 : spectrum2MzList) {
            mixMap.put(mz2, spectrum2Map.get(mz2));
        }

        //turn mixMap to mixSpectrum
        List<Double> mixMzList = new ArrayList<>(mixMap.keySet().stream().toList());
        mixMzList.sort(Double::compareTo);
        double[] mzs = new double[mixMap.keySet().size()];
        double[] ints = new double[mixMap.keySet().size()];
        for (int i = 0; i < mixMzList.size(); i++) {
            mzs[i] = mixMzList.get(i);
            ints[i] = mixMap.get(mzs[i]);
        }

        //normalize the mixSpectrum
        Spectrum mixSpectrum = new Spectrum(mzs, ints);
        normalize(mixSpectrum);
        return mixSpectrum;
    }

    public static Spectrum clone(Spectrum spectrum) {
        Spectrum copySpectrum = new Spectrum(new double[spectrum.getMzs().length], new double[spectrum.getInts().length]);
        System.arraycopy(spectrum.getMzs(), 0, copySpectrum.getMzs(), 0, spectrum.getMzs().length);
        System.arraycopy(spectrum.getInts(), 0, copySpectrum.getInts(), 0, spectrum.getInts().length);
        return copySpectrum;
    }

}
