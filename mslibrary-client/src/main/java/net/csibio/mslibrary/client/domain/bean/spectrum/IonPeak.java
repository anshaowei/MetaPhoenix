package net.csibio.mslibrary.client.domain.bean.spectrum;

import lombok.Data;

@Data
public class IonPeak {
    double mz;
    double intensity;

    public IonPeak(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }
}
