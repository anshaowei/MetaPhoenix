package net.csibio.mslibrary.client.constants.enums;

public enum SpectrumMatchMethod {

    Cosine("Cosine"),
    Entropy("Entropy"),
    Unweighted_Entropy("Unweighted_Entropy"),
    MetaPro("MetaPro");

    private final String name;

    SpectrumMatchMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
