package net.csibio.mslibrary.client.constants.enums;

public enum LibraryType {

    Genomics("Genomics","基因组学相关库"),
    Proteomics("Proteomics","蛋白质组学相关库"),
    Metabolomics("Metabolomics","代谢组学相关库"),
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
