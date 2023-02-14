package net.csibio.mslibrary.client.constants.enums;

public enum DecoyStrategy {

    Naive("Naive"),
    XYMeta("XYMeta"),
    FragmentationTree("FragmentationTree"),
    SpectrumBased("SpectrumBased");

    final String name;

    DecoyStrategy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
