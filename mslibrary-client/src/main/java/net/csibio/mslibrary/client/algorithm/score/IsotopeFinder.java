package net.csibio.mslibrary.client.algorithm.score;

import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.constants.NumericalConst;
import org.springframework.stereotype.Component;

@Component("isotopeFinder")
public class IsotopeFinder {

    public Double[] getRunDistribution(Spectrum ms1Spectrum, double monoMz, double mzTolerance, boolean isPpm, int maxIsotope) {
        double[] mzArray = ms1Spectrum.getMzs();
        double[] intensityArray = ms1Spectrum.getInts();
        Double[] expDistribution = new Double[maxIsotope];
        int iso = 0;
        double isoMass = monoMz, intensity = 0d, tolerance = mzTolerance;
        if (isPpm) {
            tolerance = monoMz * mzTolerance / NumericalConst.MILLION;
        }

        for (int i = 0; i < mzArray.length; i++) {
            // TODO Nico score accelerating
            if (mzArray[i] < isoMass - tolerance) {
                continue;
            }
            if (isoMass - tolerance <= mzArray[i] && mzArray[i] <= isoMass + tolerance) {
                intensity += intensityArray[i];
            } else {
                expDistribution[iso] = intensity;
                intensity = 0;
                iso++;
                if (iso >= maxIsotope) {
                    break;
                }
                // charge always == 1 in metabolomics data
                isoMass += NumericalConst.C13C12_MASSDIFF_U;
                if (isPpm) {
                    tolerance = isoMass * mzTolerance / NumericalConst.MILLION;
                }
                i--;
            }
        }
        return expDistribution;
    }


}
