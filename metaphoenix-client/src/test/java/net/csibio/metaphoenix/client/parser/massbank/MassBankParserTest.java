package net.csibio.metaphoenix.client.parser.massbank;

import net.csibio.aird.enums.MsLevel;
import net.csibio.metaphoenix.client.constants.enums.IonMode;
import net.csibio.metaphoenix.client.domain.Result;
import net.csibio.metaphoenix.client.domain.db.LibraryDO;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.service.LibraryService;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MassBankParserTest {

    @Mock
    SpectrumService spectrumService;
    @Mock
    LibraryService libraryService;
    @InjectMocks
    MassBankParser massBankParser;

    @TempDir
    Path dir;

    @BeforeEach
    void libraryIdIsItsName() {
        when(libraryService.insert(any(LibraryDO.class))).thenAnswer(invocation -> {
            LibraryDO library = invocation.getArgument(0);
            library.setId(library.getName());
            return Result.OK();
        });
    }

    @SuppressWarnings("unchecked")
    private List<SpectrumDO> inserted(String libraryName) {
        ArgumentCaptor<List<SpectrumDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(spectrumService).insert(captor.capture(), eq(libraryName));
        return captor.getValue();
    }

    @Test
    void parsesMassBankEuropeMsp() throws Exception {
        Path file = dir.resolve("massbank.msp");
        Files.writeString(file, """
                Name: Caffeine
                InChIKey: RYYVLZVUVIJVGH-UHFFFAOYSA-N
                Precursor_type: [M+H]+
                Spectrum_type: MS2
                PrecursorMZ: 195.0877
                Ion_mode: POSITIVE
                Num Peaks: 3
                110.0713 10.5
                138.0662 100
                195.0877 50

                Name: Caffeine
                InChIKey: RYYVLZVUVIJVGH-UHFFFAOYSA-N
                Spectrum_type: MS1
                PrecursorMZ: 195.0877
                Ion_mode: NEGATIVE
                Num Peaks: 1
                195.0877 100
                """);

        massBankParser.parseMspEU(file.toString(), "MassBank-EU");

        List<SpectrumDO> spectra = inserted("MassBank-EU");
        assertEquals(2, spectra.size());
        SpectrumDO first = spectra.get(0);
        assertEquals("RYYVLZVUVIJVGH-UHFFFAOYSA-N", first.getInChIKey());
        assertEquals(195.0877, first.getPrecursorMz(), 1e-9);
        assertEquals(MsLevel.MS2.getCode(), first.getMsLevel());
        assertEquals(IonMode.Positive.getName(), first.getIonMode());
        assertEquals("MassBank-EU", first.getLibraryId());
        assertArrayEquals(new double[]{110.0713, 138.0662, 195.0877}, first.getMzs(), 1e-9);
        assertArrayEquals(new double[]{10.5, 100, 50}, first.getInts(), 1e-9);
        assertEquals(MsLevel.MS1.getCode(), spectra.get(1).getMsLevel());
        assertEquals(IonMode.Negative.getName(), spectra.get(1).getIonMode());
    }

    @Test
    void parsesMoNAMspIntoTheGivenLibrary() throws Exception {
        Path file = dir.resolve("mona.msp");
        Files.writeString(file, """
                Name: Apigenin
                InChIKey: KZNIFHPLKGYRTM-UHFFFAOYSA-N
                Precursor_type: [M+H]+
                Spectrum_type: MS2
                PrecursorMZ: 271.0601
                Ion_mode: P
                Num Peaks: 2
                153.0182 100
                271.0601 80
                """);

        massBankParser.parseMspMoNA(file.toString(), "MoNA-2023");

        List<SpectrumDO> spectra = inserted("MoNA-2023");
        assertEquals(1, spectra.size());
        assertEquals("KZNIFHPLKGYRTM-UHFFFAOYSA-N", spectra.get(0).getInChIKey());
        assertEquals(IonMode.Positive.getName(), spectra.get(0).getIonMode());
        assertEquals("MoNA-2023", spectra.get(0).getLibraryId());
        assertArrayEquals(new double[]{153.0182, 271.0601}, spectra.get(0).getMzs(), 1e-9);
    }

    @Test
    void moNAKeepsItsDefaultLibraryName() throws Exception {
        Path file = dir.resolve("mona.msp");
        Files.writeString(file, "Name: X\nSpectrum_type: MS2\nPrecursorMZ: 100\nNum Peaks: 1\n100 1\n");

        massBankParser.parseMspMoNA(file.toString());

        assertEquals(1, inserted("MassBank-MoNA").size());
    }

    @Test
    void massBankEuropeValuesWithSpacesStayIntact() throws Exception {
        Path file = dir.resolve("massbank.msp");
        Files.writeString(file, """
                Name: Caffeine citrate
                Synon: 1,3,7-trimethyl xanthine
                Spectrum_type: MS2
                PrecursorMZ: 195.0877
                Instrument_type: LC-ESI-QTOF
                Instrument: Bruker maXis Impact
                Comments: "computed SMILES" from structure
                Num Peaks: 2
                110.0713	10.5
                195.0877  50
                """);

        massBankParser.parseMspEU(file.toString(), "MassBank-EU");

        SpectrumDO spectrum = inserted("MassBank-EU").get(0);
        assertEquals("Caffeine citrate", spectrum.getCompoundName());
        assertEquals("1,3,7-trimethyl xanthine", spectrum.getSynon());
        assertEquals("Bruker maXis Impact", spectrum.getInstrument());
        assertEquals("\"computed SMILES\" from structure", spectrum.getComment());
        assertArrayEquals(new double[]{110.0713, 195.0877}, spectrum.getMzs(), 1e-9);
        assertArrayEquals(new double[]{10.5, 50}, spectrum.getInts(), 1e-9);
    }

    @Test
    void moNAValuesWithSpacesStayIntactAndSmilesComesFromTheComments() throws Exception {
        Path file = dir.resolve("mona.msp");
        Files.writeString(file, """
                Name: Apigenin 7-glucoside
                Spectrum_type: MS2
                PrecursorMZ: 433.1129
                Instrument: Thermo Q Exactive HF
                Comments: "SMILES=OC1=CC=CC=C1" "computed InChI=x"
                Num Peaks: 1
                271.0601 100
                """);

        massBankParser.parseMspMoNA(file.toString(), "MoNA");

        SpectrumDO spectrum = inserted("MoNA").get(0);
        assertEquals("Apigenin 7-glucoside", spectrum.getCompoundName());
        assertEquals("Thermo Q Exactive HF", spectrum.getInstrument());
        assertEquals("OC1=CC=CC=C1", spectrum.getSmiles());
    }
}
