package net.csibio.mslibrary.client.constants.enums;

public enum DecoyProcedure {

    //separated target-decoy search
    STDS("STDS"),
    //concatenated target-decoy competition
    CTDC("CTDC"),
    //target-only target-decoy competition
    TTDC("TTDC"),
    //traditional strategy
    Common("Common"),
    //mix-max
    Mix_Max("Mix_Max");

    final String name;

    DecoyProcedure(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
