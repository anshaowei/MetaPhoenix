package net.csibio.mslibrary.client.constants.enums;

public enum LibraryType {

    INS("INS", "Internal Standard"),
    ANA("ANA", "Analytes to analyze"),
    ;

    String name;

    String description;

    LibraryType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static LibraryType getByName(String name) {
        for (LibraryType libraryType: values()) {
            if (libraryType.getName().equals(name)) {
                return libraryType;
            }
        }
        return null;
    }

    public static LibraryType getByName(String name, LibraryType defaultType) {
        for (LibraryType libraryType: values()) {
            if (libraryType.getName().equals(name)) {
                return libraryType;
            }
        }
        return defaultType;
    }

    public String getName() {
        return this.name;
    }
}
