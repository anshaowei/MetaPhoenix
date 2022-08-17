package net.csibio.mslibrary.client.domain.bean.spectrum;

import lombok.Data;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;

@Data
public class SimpleSpectrum {

    double[] mzs;
    double[] ints;

    public SimpleSpectrum(SpectrumDO spectrumDO) {
        this.mzs = spectrumDO.getMzs();
        this.ints = spectrumDO.getInts();
    }

    public SimpleSpectrum() {
    }


}
