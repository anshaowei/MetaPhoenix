package net.csibio.metaphoenix.core.config;

import org.springframework.data.convert.SimpleTypeInformationMapper;
import org.springframework.data.convert.TypeInformationMapper;
import org.springframework.data.core.TypeInformation;
import org.springframework.data.mapping.Alias;

/**
 * Resolves the "_class" type hint of documents written before the project was renamed from MSLibrary to MetaPhoenix.
 * Those documents store class names under net.csibio.mslibrary.*, which are mapped to net.csibio.metaphoenix.* on read.
 * New documents are always written with the current class names.
 */
public class LegacyTypeInformationMapper implements TypeInformationMapper {

    public static final String LEGACY_PACKAGE = "net.csibio.mslibrary.";
    public static final String CURRENT_PACKAGE = "net.csibio.metaphoenix.";

    private final SimpleTypeInformationMapper delegate = new SimpleTypeInformationMapper();

    @Override
    public TypeInformation<?> resolveTypeFrom(Alias alias) {
        if (alias.getValue() instanceof String typeName && typeName.startsWith(LEGACY_PACKAGE)) {
            alias = Alias.of(CURRENT_PACKAGE + typeName.substring(LEGACY_PACKAGE.length()));
        }
        return delegate.resolveTypeFrom(alias);
    }

    @Override
    public Alias createAliasFor(TypeInformation<?> type) {
        return delegate.createAliasFor(type);
    }
}
