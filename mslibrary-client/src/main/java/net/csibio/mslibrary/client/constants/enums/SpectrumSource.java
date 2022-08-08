package net.csibio.mslibrary.client.constants.enums;

public enum SpectrumSource {

    Deconvoluted("Deconvoluted"),
    Standard("Standard"),
    Raw("Raw"),
    ;

    String name;

    SpectrumSource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
