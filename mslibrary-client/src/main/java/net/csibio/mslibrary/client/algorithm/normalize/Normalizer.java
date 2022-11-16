package net.csibio.mslibrary.client.algorithm.normalize;

import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import org.springframework.stereotype.Component;

@Component("normalizer")
public class Normalizer {

    public SpectrumDO getNormalizedSpectrum(SpectrumDO spectrumDO) {
        Spectrum spectrum = new Spectrum(spectrumDO.getMzs(), spectrumDO.getInts());
        Spectrum normalizedSpectrum = getNormalizedSpectrum(spectrum);
        spectrumDO.setMzs(normalizedSpectrum.getMzs());
        spectrumDO.setInts(normalizedSpectrum.getInts());
        return spectrumDO;
    }

    public Spectrum getNormalizedSpectrum(Spectrum spectrum) {
        Spectrum normalizedSpectrum = new Spectrum(spectrum.getMzs(), spectrum.getInts());
        double sum = ArrayUtil.sum(normalizedSpectrum.getInts());
        ArrayUtil.normalize(normalizedSpectrum.getInts(), sum);
        return normalizedSpectrum;
    }

}
