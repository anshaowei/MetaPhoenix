package net.csibio.metaphoenix.core.components;

import net.csibio.metaphoenix.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.metaphoenix.client.algorithm.identification.Identify;
import net.csibio.metaphoenix.client.constants.enums.DecoyStrategy;
import net.csibio.metaphoenix.client.domain.db.MethodDO;
import net.csibio.metaphoenix.client.domain.query.SpectrumQuery;
import net.csibio.metaphoenix.client.filter.NoiseFilter;
import net.csibio.metaphoenix.client.parser.common.MgfParser;
import net.csibio.metaphoenix.client.parser.common.MspParser;
import net.csibio.metaphoenix.client.parser.common.MzMLParser;
import net.csibio.metaphoenix.client.parser.massbank.MassBankParser;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandRunnerTest {

    @Mock
    SpectrumService spectrumService;
    @Mock
    SpectrumGenerator spectrumGenerator;
    @Mock
    NoiseFilter noiseFilter;
    @Mock
    MspParser mspParser;
    @Mock
    MassBankParser massBankParser;
    @Mock
    MzMLParser mzMLParser;
    @Mock
    Identify identify;
    @Mock
    MgfParser mgfParser;
    @InjectMocks
    CommandRunner commandRunner;

    private void run(String... lines) {
        commandRunner.run(new BufferedReader(new StringReader(String.join("\n", lines))));
    }

    private MethodDO captureDecoyMethod(String library, boolean overwrite) {
        ArgumentCaptor<MethodDO> captor = ArgumentCaptor.forClass(MethodDO.class);
        verify(spectrumGenerator).execute(eq(library), captor.capture(), eq(overwrite));
        return captor.getValue();
    }

    @Test
    void parseOptionsReadsValuesAndFlags() {
        Map<String, String> options = CommandRunner.parseOptions("decoy -l lib -seed 42 -overwrite -ppm 5".split(" "));

        assertEquals(Map.of("-l", "lib", "-seed", "42", "-overwrite", "true", "-ppm", "5"), options);
    }

    @Test
    void parseOptionsRejectsMissingValuesAndStrayArguments() {
        assertThrows(IllegalArgumentException.class, () -> CommandRunner.parseOptions("decoy -l".split(" ")));
        assertThrows(IllegalArgumentException.class, () -> CommandRunner.parseOptions("decoy lib".split(" ")));
    }

    @Test
    void buildMethodUsesDefaultsAndOverrides() {
        MethodDO defaults = CommandRunner.buildMethod(new HashMap<>());
        assertEquals(10, defaults.getPpm());
        assertTrue(defaults.getPpmForMzTolerance());
        assertEquals(DecoyStrategy.IonEntropyBased.getName(), defaults.getDecoyStrategy());
        assertNull(defaults.getRandomSeed());

        MethodDO custom = CommandRunner.buildMethod(Map.of("-s", "XYMeta", "-ppm", "20", "-seed", "7"));
        assertEquals(20, custom.getPpm());
        assertEquals("XYMeta", custom.getDecoyStrategy());
        assertEquals(7L, custom.getRandomSeed());
    }

    @Test
    void buildMethodRejectsUnknownStrategy() {
        assertThrows(IllegalArgumentException.class, () -> CommandRunner.buildMethod(Map.of("-s", "Shuffle")));
    }

    @Test
    void decoyPassesStrategyPpmSeedAndOverwrite() {
        run("decoy -l lib -s SpectralEntropyBased -ppm 5 -seed 42 -overwrite");

        MethodDO method = captureDecoyMethod("lib", true);
        assertEquals("SpectralEntropyBased", method.getDecoyStrategy());
        assertEquals(5, method.getPpm());
        assertEquals(42L, method.getRandomSeed());
    }

    @Test
    void decoyDefaultsToIonEntropyWithoutOverwrite() {
        run("decoy -l lib");

        MethodDO method = captureDecoyMethod("lib", false);
        assertEquals(DecoyStrategy.IonEntropyBased.getName(), method.getDecoyStrategy());
        assertNull(method.getRandomSeed());
    }

    @Test
    void badCommandsAreReportedAndTheRunnerKeepsGoing() {
        // missing parameter, missing value, unknown strategy, invalid number, unknown command, empty line, then a valid command
        run("decoy", "decoy -l", "decoy -l lib -s Shuffle", "decoy -l lib -ppm ten", "shuffle -l lib", "", "filter -l lib");

        verify(spectrumGenerator, never()).execute(anyString(), any(), anyBoolean());
        verify(noiseFilter).filter("lib");
    }

    @Test
    void runnerStopsWhenInputIsClosed() {
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(5), () -> run());
    }

    @Test
    void importAndFilterRequireTheirParameters() {
        run("import -f lib.msp", "import -f lib.msp -l lib", "filter");

        verify(mspParser).execute("lib.msp", "lib");
        verifyNoInteractions(noiseFilter);
    }

    @Test
    void importChoosesTheParserByFormat() {
        run("import -f a.msp -l a", "import -f ALL_GNPS.msp -l ALL_GNPS -t gnps", "import -f eu.msp -l eu -t massbank", "import -f mona.msp -l mona -t mona", "import -f x.msp -l x -t nist");

        verify(mspParser).execute("a.msp", "a");
        verify(mspParser).execute("ALL_GNPS.msp", "ALL_GNPS");
        verify(massBankParser).parseMspEU("eu.msp", "eu");
        verify(massBankParser).parseMspMoNA("mona.msp", "mona");
        verifyNoMoreInteractions(mspParser, massBankParser);
    }

    @Test
    void identifyStopsWhenTheDecoyLibraryIsMissing() {
        when(spectrumService.exists(any(SpectrumQuery.class), eq("lib"))).thenReturn(true);
        when(spectrumService.exists(any(SpectrumQuery.class), eq("lib-XYMeta"))).thenReturn(false);

        run("identify -f query.mgf -l lib -fdr 0.05 -s XYMeta");

        verifyNoInteractions(mgfParser, identify);
    }

    @Test
    void identifyStopsWhenTheTargetLibraryIsMissing() {
        when(spectrumService.exists(any(SpectrumQuery.class), eq("lib"))).thenReturn(false);

        run("identify -f query.mgf -l lib -fdr 0.05");

        verifyNoInteractions(mgfParser, identify);
    }

    @Test
    void identifyRejectsUnsupportedFileFormats() {
        when(spectrumService.exists(any(SpectrumQuery.class), anyString())).thenReturn(true);

        run("identify -f query.txt -l lib -fdr 0.05");

        verifyNoInteractions(mgfParser, mzMLParser, identify);
    }

    @Test
    void identifyUsesTheChosenDecoyLibraryAndWritesTheReport() throws Exception {
        when(spectrumService.exists(any(SpectrumQuery.class), anyString())).thenReturn(true);
        when(mgfParser.execute("query.mgf")).thenReturn(new ArrayList<>());
        when(identify.execute(anyList(), eq("lib"), eq("lib-SpectralEntropyBased"), any(), eq(0.01))).thenReturn(new HashMap<>());
        java.nio.file.Path report = java.nio.file.Files.createTempFile("report", ".xlsx");

        run("identify -f query.mgf -l lib -fdr 0.01 -s SpectralEntropyBased -o " + report);

        verify(identify).execute(anyList(), eq("lib"), eq("lib-SpectralEntropyBased"), any(), eq(0.01));
        assertTrue(java.nio.file.Files.size(report) > 0);
        java.nio.file.Files.deleteIfExists(report);
    }
}
