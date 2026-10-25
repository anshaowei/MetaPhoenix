package net.csibio.metaphoenix.client.domain.bean.spectrum;

import lombok.Data;

@Data
public class IonPeak implements Comparable {
    Double mz;
    Double intensity;

    Double ionEntropy;

    public IonPeak(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj instanceof IonPeak) {
            IonPeak ionPeak = (IonPeak) obj;
            return mz.compareTo(ionPeak.mz);
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof IonPeak) {
            IonPeak other = (IonPeak) obj;
            if (other.getMz() == null || this.getMz() == null) {
                return false;
            }
            if (other.getIntensity() == null || this.getIntensity() == null) {
                return false;
            }
            return this.getMz().equals(other.getMz()) && this.getIntensity().equals(other.getIntensity());

        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getMz() != null && this.getIntensity() != null) {
            return this.getMz().hashCode() + this.getIntensity().hashCode();
        } else {
            return super.hashCode();
        }
    }
}
