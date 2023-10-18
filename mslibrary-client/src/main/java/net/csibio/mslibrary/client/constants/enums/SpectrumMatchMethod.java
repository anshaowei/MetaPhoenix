package net.csibio.mslibrary.client.constants.enums;

public enum SpectrumMatchMethod {

    Cosine("Cosine"),
    Cosine_SquareRoot("Cosine_ SquareRoot"),
    Entropy("Entropy"),
    Unweighted_Entropy("Unweighted_Entropy"),
    MetaPro("MetaPro"),
    Weighted_Cosine("Weighted_Cosine");

    private final String name;

    SpectrumMatchMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
