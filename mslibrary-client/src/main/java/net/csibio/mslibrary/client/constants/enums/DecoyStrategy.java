package net.csibio.mslibrary.client.constants.enums;

public enum DecoyStrategy {

    Naive("Naive"),
    XYMeta("XYMeta"),
    SpectrumBased("SpectrumBased"),
    Entropy_1("Entropy_1"),
    Entropy_2("Entropy_2");

    final String name;

    DecoyStrategy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
