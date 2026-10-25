package net.csibio.metaphoenix.client.algorithm.decoy.generator;

import net.csibio.metaphoenix.client.domain.db.SpectrumDO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Sorted (m/z, spectrum) pairs of a library, to find the spectra with an m/z in a window by binary search instead of
 * scanning the whole library. m/z values are quantized, so the index returns a superset of the matching spectra and
 * {@link #find} applies the caller's exact condition. Matches are returned in library order, so every result is the
 * same as with a full scan of the library.
 */
class MzIndex {

    private static final double SCALE = 1e5;
    private static final int INDEX_BITS = 24;
    private static final long INDEX_MASK = (1L << INDEX_BITS) - 1;
    private static final double MAX_SCALED_MZ = (double) (1L << (63 - INDEX_BITS));

    private final List<SpectrumDO> spectra;

    // (quantized m/z << INDEX_BITS | spectrum index), sorted; null if the library cannot be indexed and is scanned instead
    private final long[] keys;

    private MzIndex(List<SpectrumDO> spectra, Function<SpectrumDO, double[]> mzsOf) {
        this.spectra = spectra;
        this.keys = buildKeys(spectra, mzsOf);
    }

    static MzIndex ofPrecursors(List<SpectrumDO> spectra) {
        return new MzIndex(spectra, spectrum -> spectrum.getPrecursorMz() == null ? new double[0] : new double[]{spectrum.getPrecursorMz()});
    }

    static MzIndex ofPeaks(List<SpectrumDO> spectra) {
        return new MzIndex(spectra, SpectrumDO::getMzs);
    }

    private static long[] buildKeys(List<SpectrumDO> spectra, Function<SpectrumDO, double[]> mzsOf) {
        if (spectra.size() > INDEX_MASK) {
            return null;
        }
        List<double[]> mzsList = new ArrayList<>(spectra.size());
        int count = 0;
        for (SpectrumDO spectrum : spectra) {
            double[] mzs = mzsOf.apply(spectrum);
            mzsList.add(mzs);
            count += mzs.length;
        }
        long[] keys = new long[count];
        int k = 0;
        for (int i = 0; i < spectra.size(); i++) {
            for (double mz : mzsList.get(i)) {
                double scaled = mz * SCALE;
                if (!(scaled >= 0 && scaled < MAX_SCALED_MZ)) {
                    return null;
                }
                keys[k++] = ((long) scaled << INDEX_BITS) | i;
            }
        }
        Arrays.parallelSort(keys);
        return keys;
    }

    /**
     * @return the spectra matching the condition among those with an indexed m/z within [mz - window, mz + window],
     * in library order. The condition must only accept spectra with an indexed m/z in that window.
     */
    List<SpectrumDO> find(double mz, double window, Predicate<SpectrumDO> condition) {
        if (keys == null) {
            return spectra.stream().filter(condition).toList();
        }
        // one quantization step of margin on each side, so no spectrum in the window is missed
        long low = Math.max(0, (long) Math.floor((mz - window) * SCALE) - 1);
        long high = (long) Math.floor((mz + window) * SCALE) + 1;
        int from = lowerBound(low << INDEX_BITS);
        int to = high + 1 < MAX_SCALED_MZ ? lowerBound((high + 1) << INDEX_BITS) : keys.length;
        int[] indices = new int[to - from];
        for (int k = from; k < to; k++) {
            indices[k - from] = (int) (keys[k] & INDEX_MASK);
        }
        Arrays.sort(indices);
        List<SpectrumDO> result = new ArrayList<>();
        for (int k = 0; k < indices.length; k++) {
            if (k > 0 && indices[k] == indices[k - 1]) {
                continue;
            }
            SpectrumDO spectrum = spectra.get(indices[k]);
            if (condition.test(spectrum)) {
                result.add(spectrum);
            }
        }
        return result;
    }

    private int lowerBound(long key) {
        int index = Arrays.binarySearch(keys, key);
        if (index < 0) {
            return -index - 1;
        }
        while (index > 0 && keys[index - 1] == key) {
            index--;
        }
        return index;
    }
}
