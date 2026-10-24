package net.csibio.mslibrary.client.parser.common;

import net.csibio.aird.enums.MsLevel;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MspParserTest {

    @Mock
    LibraryService libraryService;
    @Mock
    SpectrumService spectrumService;
    @InjectMocks
    MspParser parser;

    @TempDir
    Path dir;

    private static final String MSP = String.join("\n",
            "Name: Caffeine",
            "PrecursorMZ: 195.0877",
            "PrecursorType: [M+H]+",
            "Formula: C8H10N4O2",
            "InChIKey: RYYVLZVUVIJVGH-UHFFFAOYSA-N",
            "InChI: InChI=1S/C8H10N4O2",
            "Ionmode: Positive",
            "Instrument: Orbitrap",
            "InstrumentType: LC-ESI-ITFT",
            "CollisionEnergy: 35",
            "Num Peaks: 2",
            "138.0662\t100.0",
            "110.0713\t20.5",
            "",
            "Name: Second",
            "PrecursorMZ: 300.1",
            "Ionmode: Negative",
            "Num Peaks: 1",
            "100.0\t50.0",
            "");

    @Test
    void importsSpectraWithMetadataAndPeaks() throws IOException {
        Path file = dir.resolve("lib.msp");
        Files.writeString(file, MSP);
        when(libraryService.insert(any(LibraryDO.class))).thenReturn(Result.OK());

        parser.execute(file.toString(), "testLib");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SpectrumDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(spectrumService).insert(captor.capture(), any());
        List<SpectrumDO> spectra = captor.getValue();
        assertEquals(2, spectra.size());

        SpectrumDO s = spectra.get(0);
        assertEquals("Caffeine", s.getCompoundName());
        assertEquals(195.0877, s.getPrecursorMz(), 1e-9);
        assertEquals("[M+H]+", s.getPrecursorAdduct());
        assertEquals("C8H10N4O2", s.getFormula());
        assertEquals("RYYVLZVUVIJVGH-UHFFFAOYSA-N", s.getInChIKey());
        assertEquals("InChI=1S/C8H10N4O2", s.getInChI());
        assertEquals("Positive", s.getIonMode());
        assertEquals("Orbitrap", s.getInstrument());
        assertEquals("LC-ESI-ITFT", s.getInstrumentType());
        assertEquals(35.0, s.getCollisionEnergy(), 1e-9);
        assertEquals(MsLevel.MS2.getCode(), s.getMsLevel());
        assertArrayEquals(new double[]{138.0662, 110.0713}, s.getMzs(), 1e-9);
        assertArrayEquals(new double[]{100.0, 20.5}, s.getInts(), 1e-9);

        assertEquals("Negative", spectra.get(1).getIonMode());
        assertEquals(1, spectra.get(1).getMzs().length);
    }

    @Test
    void doesNotImportWhenLibraryCreationFails() throws IOException {
        Path file = dir.resolve("lib.msp");
        Files.writeString(file, MSP);
        when(libraryService.insert(any(LibraryDO.class))).thenReturn(new Result<>(false));

        parser.execute(file.toString(), "testLib");

        verify(spectrumService, never()).insert(any(List.class), anyString());
    }
}
