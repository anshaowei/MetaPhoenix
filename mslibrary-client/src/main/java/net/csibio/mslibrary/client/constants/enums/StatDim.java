package net.csibio.mslibrary.client.constants.enums;

public enum StatDim {

    Day("Day"),
    Week("Week"),
    Month("Month"),
    Year("Year"),
    ;

    private final String dim;

    StatDim(String dim) {
        this.dim = dim;
    }

    public String getDim() {
        return dim;
    }
}
