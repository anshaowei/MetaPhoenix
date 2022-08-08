package net.csibio.mslibrary.client.constants.enums;

public enum StatType {

    Global_Total("Global_Total"),
    Library("Library");

    String name;

    StatType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
