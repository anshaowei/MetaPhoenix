package net.csibio.mslibrary.client.constants.enums;

public enum IonMode {

    Positive("Positive"),
    Negative("Negative"),
    ;

    String name;

    IonMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
