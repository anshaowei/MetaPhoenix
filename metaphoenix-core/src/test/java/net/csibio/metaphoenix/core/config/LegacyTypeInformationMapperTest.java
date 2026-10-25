package net.csibio.metaphoenix.core.config;

import net.csibio.metaphoenix.client.domain.db.LibraryDO;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.Alias;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import static org.junit.jupiter.api.Assertions.*;

class LegacyTypeInformationMapperTest {

    private MappingMongoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, new MongoMappingContext());
        MongoConfig.legacyTypeMapperPostProcessor().postProcessBeforeInitialization(converter, "mappingMongoConverter");
        converter.afterPropertiesSet();
    }

    @Test
    void readsDocumentWithLegacyClassName() {
        Document document = new Document("_class", "net.csibio.mslibrary.client.domain.db.SpectrumDO")
                .append("precursorMz", 271.06062)
                .append("inChIKey", "KZNIFHPLKGYRTM-UHFFFAOYSA-N");

        // reading as Object only succeeds if the type hint itself is resolved
        Object result = converter.read(Object.class, document);

        SpectrumDO spectrum = assertInstanceOf(SpectrumDO.class, result);
        assertEquals(271.06062, spectrum.getPrecursorMz());
        assertEquals("KZNIFHPLKGYRTM-UHFFFAOYSA-N", spectrum.getInChIKey());
    }

    @Test
    void readsDocumentWithCurrentClassName() {
        Document document = new Document("_class", LibraryDO.class.getName()).append("name", "ALL_GNPS");

        LibraryDO library = assertInstanceOf(LibraryDO.class, converter.read(Object.class, document));
        assertEquals("ALL_GNPS", library.getName());
    }

    @Test
    void writesCurrentClassName() {
        SpectrumDO spectrum = new SpectrumDO();
        spectrum.setPrecursorMz(100d);
        Document document = new Document();

        converter.write(spectrum, document);

        assertEquals(SpectrumDO.class.getName(), document.get("_class"));
    }

    @Test
    void unknownLegacyClassResolvesToNothing() {
        LegacyTypeInformationMapper mapper = new LegacyTypeInformationMapper();

        assertNull(mapper.resolveTypeFrom(Alias.of("net.csibio.mslibrary.client.domain.db.ProteinDO")));
    }
}
