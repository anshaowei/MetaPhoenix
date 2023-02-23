package net.csibio.mslibrary.client.utils;

import net.csibio.aird.bean.common.Spectrum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpectrumUtil {

    public static void normalize(Spectrum spectrum) {
        double sum = ArrayUtil.sum(spectrum.getInts());
        ArrayUtil.normalize(spectrum.getInts(), sum);
    }

    /**
     * 根据权重混合两个谱图
     *
     * @param spectrum1
     * @param spectrum2
     * @param weight1
     * @param weight2
     * @param mzTolerance
     * @return
     */
    public static Spectrum mixByWeight(Spectrum spectrum1, Spectrum spectrum2, double weight1, double weight2, double mzTolerance) {

        HashMap<Double, Double> mixMap = new HashMap<>();
        HashMap<Double, Double> map2 = new HashMap<>();
        List<Double> mz2List = new ArrayList<>();
        for (int i = 0; i < spectrum2.getMzs().length; i++) {
            mz2List.add(spectrum2.getMzs()[i]);
            map2.put(spectrum2.getMzs()[i], spectrum2.getInts()[i]);
        }

        //遍历第一张谱图，找到第二张谱图中的匹配项并混合
        for (int i = 0; i < spectrum1.getMzs().length; i++) {
            double mz1 = spectrum1.getMzs()[i];
            List<Double> mz2ListInTolerance = mz2List.stream().filter(mz2 -> Math.abs(mz1 - mz2) <= mzTolerance).toList();
            if (mz2ListInTolerance.size() > 0) {
                double diff = Double.MAX_VALUE;
                double target = 0;
                for (double mz2 : mz2ListInTolerance) {
                    double tempDiff = Math.abs(mz1 - mz2);
                    if (tempDiff < diff) {
                        target = mz2;
                        diff = tempDiff;
                    }
                }
                double mixMz = (mz1 * weight1 + target * weight2) / (weight1 + weight2);
                double mixInt = (spectrum1.getInts()[i] * weight1 + map2.get(target) * weight2) / (weight1 + weight2);
                mixMap.put(mixMz, mixInt);
                mz2List.remove(target);
            } else {
                mixMap.put(mz1, spectrum1.getInts()[i]);
            }
        }

        //将剩余的第二张谱图的项加入混合谱图
        for (double mz2 : mz2List) {
            mixMap.put(mz2, map2.get(mz2));
        }

        //将mixMap转化为谱图
        List<Double> mixMzList = new ArrayList<>(mixMap.keySet().stream().toList());
        mixMzList.sort(Double::compareTo);
        double[] mzs = new double[mixMap.keySet().size()];
        double[] ints = new double[mixMap.keySet().size()];
        for (int i = 0; i < mixMzList.size(); i++) {
            mzs[i] = mixMzList.get(i);
            ints[i] = mixMap.get(mzs[i]);
        }

        //mix谱图信号归一化
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
