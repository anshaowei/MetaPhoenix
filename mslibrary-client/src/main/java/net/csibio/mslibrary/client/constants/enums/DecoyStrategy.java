package net.csibio.mslibrary.client.constants.enums;

public enum DecoyStrategy {

    Naive("Naive"),
    XYMeta("XYMeta"),
    SpectrumBased("SpectrumBased"),
    SpectralEntropyBased("SpectralEntropyBased"),
    IonEntropyBased("IonEntropyBased"),
    FragmentationTreeBased("FragmentationTreeBased"),
    IonEntropyBased2("IonEntropyBased2");

    private final String name;

    DecoyStrategy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
