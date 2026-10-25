package net.csibio.metaphoenix.client.constants.enums;

public enum IonMode {

    Positive("Positive"),
    Negative("Negative"),
    ;

    private final String name;

    IonMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
