package net.csibio.metaphoenix.client.constants.enums;

public enum LibraryType {

    Metabolomics("Metabolomics", "代谢组学相关库"),
    ;

    private final String name;

    private final String description;

    LibraryType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static LibraryType getByName(String name) {
        for (LibraryType libraryType : values()) {
            if (libraryType.getName().equals(name)) {
                return libraryType;
            }
        }
        return null;
    }

    public static LibraryType getByName(String name, LibraryType defaultType) {
        for (LibraryType libraryType : values()) {
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
