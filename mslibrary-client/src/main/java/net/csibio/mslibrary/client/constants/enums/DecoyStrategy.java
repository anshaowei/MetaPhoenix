package net.csibio.mslibrary.client.constants.enums;

public enum DecoyStrategy {

    Naive("Naive"),
    XYMeta("XYMeta"),
//    SpectrumBased("SpectrumBased"),
    SpectralEntropyBased("SpectralEntropyBased"),
    SpectralEntropyBased_PR("SpectralEntropyBased_PR"),
    IonEntropyBased("IonEntropyBased"),
    IonEntropyBased_PR("IonEntropyBased_PR"),
//    IonEntropyBased_BasePeak("IonEntropyBased_BasePeak"),
    FragmentationTreeBased("FragmentationTreeBased");


    private final String name;

    DecoyStrategy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
