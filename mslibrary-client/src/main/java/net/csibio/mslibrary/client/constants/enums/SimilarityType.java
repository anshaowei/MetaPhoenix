package net.csibio.mslibrary.client.constants.enums;

public enum SimilarityType {

    Cosine("Cosine"),
    Entropy("Entropy"),
    Unweighted_Entropy("Unweighted_Entropy");

    final String name;

    SimilarityType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
