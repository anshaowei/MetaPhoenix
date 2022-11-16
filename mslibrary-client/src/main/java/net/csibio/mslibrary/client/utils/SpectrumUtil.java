package net.csibio.mslibrary.client.utils;

import net.csibio.aird.bean.common.Spectrum;

import java.util.HashMap;
import java.util.TreeSet;

public class SpectrumUtil {

    public static void normalize(Spectrum spectrum) {
        double sum = ArrayUtil.sum(spectrum.getInts());
        ArrayUtil.normalize(spectrum.getInts(), sum);
    }

    public static Spectrum mix(Spectrum spectrumA, Spectrum spectrumB) {
        normalize(spectrumA);
        normalize(spectrumB);

        HashMap<Double, Double> mapA = new HashMap<>();
        HashMap<Double, Double> mapB = new HashMap<>();
        for (int i = 0; i < spectrumA.getMzs().length; i++) {
            mapA.put(spectrumA.getMzs()[i], spectrumA.getInts()[i]);
        }
        for (int i = 0; i < spectrumB.getMzs().length; i++) {
            mapB.put(spectrumB.getMzs()[i], spectrumB.getInts()[i]);
        }

        TreeSet<Double> mzSet = new TreeSet<>();
        for (double mz1 : spectrumA.getMzs()) {
            mzSet.add(mz1);
        }
        for (double mz1 : spectrumB.getMzs()) {
            mzSet.add(mz1);
        }

        double[] mzs = new double[mzSet.size()];
        double[] ints = new double[mzSet.size()];
        for (int i = 0; i < mzs.length; i++) {
            mzs[i] = mzSet.pollFirst();
            ints[i] = (mapA.getOrDefault(mzs[i], 0.0) + mapB.getOrDefault(mzs[i], 0.0)) / 2;
        }

        return new Spectrum(mzs, ints);
    }

}
