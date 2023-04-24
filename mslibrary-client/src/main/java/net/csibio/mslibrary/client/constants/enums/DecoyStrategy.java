package net.csibio.mslibrary.client.constants.enums;

public enum DecoyStrategy {

    Naive("Naive"),
    XYMeta("XYMeta"),
    SpectrumBased("SpectrumBased"),
    SameMz("SameMz"),
    IonEntropy("IonEntropy"),
    FragmentationTree("FragmentationTree");

    private final String name;

    DecoyStrategy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
