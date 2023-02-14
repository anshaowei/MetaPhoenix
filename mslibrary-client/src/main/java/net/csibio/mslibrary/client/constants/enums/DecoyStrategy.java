package net.csibio.mslibrary.client.constants.enums;

public enum DecoyStrategy {

    naive("naive"),
    XYMeta("XYMeta"),
    fragmentationTree("fragmentationTree"),
    spectrumBased("spectrumBased");

    final String name;

    DecoyStrategy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
