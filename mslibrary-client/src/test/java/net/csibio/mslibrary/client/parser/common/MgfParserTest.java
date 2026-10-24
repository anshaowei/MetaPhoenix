package net.csibio.mslibrary.client.parser.common;

import net.csibio.aird.enums.MsLevel;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MgfParserTest {

    private final MgfParser parser = new MgfParser();

    @TempDir
    Path dir;

    private String parseFile(String content) throws IOException {
        Path file = dir.resolve("test.mgf");
        Files.writeString(file, content);
        return file.toString();
    }

    @Test
    void parsesPrecursorChargeAndPeaks() throws IOException {
        String path = parseFile("""
                BEGIN IONS
                PEPMASS=100.5 5000
                CHARGE=2+
                SMILES=CCO
                50.0 10.0
                60.5 20.0
                END IONS
                """);
        List<SpectrumDO> spectra = parser.execute(path);

        assertEquals(1, spectra.size());
        SpectrumDO s = spectra.get(0);
        assertEquals(100.5, s.getPrecursorMz(), 1e-9);
        assertEquals(2, s.getCharge());
        assertEquals("CCO", s.getSmiles());
        assertEquals(MsLevel.MS2.getCode(), s.getMsLevel());
        assertArrayEquals(new double[]{50.0, 60.5}, s.getMzs(), 1e-9);
        assertArrayEquals(new double[]{10.0, 20.0}, s.getInts(), 1e-9);
    }

    @Test
    void parsesNegativeChargeAndTabSeparatedPeaks() throws IOException {
        String path = parseFile("BEGIN IONS\nPEPMASS 200.0\nCHARGE 1-\n70.0\t5.0\nEND IONS\n");
        SpectrumDO s = parser.execute(path).get(0);

        assertEquals(-1, s.getCharge());
        assertArrayEquals(new double[]{70.0}, s.getMzs(), 1e-9);
        assertArrayEquals(new double[]{5.0}, s.getInts(), 1e-9);
    }

    @Test
    void parsesMultipleSpectra() throws IOException {
        String path = parseFile("""
                BEGIN IONS
                PEPMASS 100.0
                50.0 1.0
                END IONS

                BEGIN IONS
                PEPMASS 200.0
                60.0 2.0
                70.0 3.0
                END IONS
                """);
        List<SpectrumDO> spectra = parser.execute(path);

        assertEquals(2, spectra.size());
        assertEquals(100.0, spectra.get(0).getPrecursorMz(), 1e-9);
        assertEquals(2, spectra.get(1).getMzs().length);
    }

    @Test
    void dropsSpectrumWithoutPrecursor() throws IOException {
        String path = parseFile("BEGIN IONS\nSMILES CCO\n50.0 1.0\nEND IONS\n");
        assertTrue(parser.execute(path).isEmpty());
    }

    @Test
    void returnsNullForMissingFile() {
        assertNull(parser.execute(dir.resolve("missing.mgf").toString()));
    }
}
