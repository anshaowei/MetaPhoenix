package net.csibio.mslibrary.client.constants.enums;

public enum SpectrumType {

    MS1("MS1"),
    MS2("MS2"),
    ;

    String name;

    SpectrumType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
