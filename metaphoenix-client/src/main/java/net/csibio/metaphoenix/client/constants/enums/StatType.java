package net.csibio.metaphoenix.client.constants.enums;

public enum StatType {

    Global_Total("Global_Total"),
    Library("Library");

    private final String name;

    StatType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
