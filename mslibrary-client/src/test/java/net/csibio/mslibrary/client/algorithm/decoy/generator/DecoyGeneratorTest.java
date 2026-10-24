package net.csibio.mslibrary.client.algorithm.decoy.generator;

import net.csibio.mslibrary.client.domain.bean.formula.FormulaCalculator;
import net.csibio.mslibrary.client.domain.bean.formula.FragmentFactory;
import net.csibio.mslibrary.client.domain.bean.peptide.Annotation;
import net.csibio.mslibrary.client.domain.bean.peptide.FragmentInfo;
import net.csibio.mslibrary.client.domain.db.PeptideDO;
import net.csibio.mslibrary.client.parser.dia.LibraryTsvParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

class DecoyGeneratorTest {

    private static final double MOCK_MZ = 123.456;

    private final ShuffleGenerator shuffle = new ShuffleGenerator();
    private final ReplaceGenerator replace = new ReplaceGenerator();

    @BeforeEach
    void wire() {
        FormulaCalculator calculator = mock(FormulaCalculator.class);
        lenient().when(calculator.getMonoMz(anyString(), anyString(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyDouble(), org.mockito.ArgumentMatchers.anyBoolean(), org.mockito.ArgumentMatchers.anyList())).thenReturn(MOCK_MZ);

        //annotations are written as "<type><location>", e.g. "y3"
        LibraryTsvParser parser = mock(LibraryTsvParser.class);
        lenient().when(parser.parseAnnotation(anyString())).thenAnswer(inv -> {
            String text = inv.getArgument(0);
            Annotation annotation = new Annotation();
            annotation.setType(text.substring(0, 1));
            annotation.setLocation(Integer.parseInt(text.substring(1)));
            return annotation;
        });

        FragmentFactory factory = new FragmentFactory();
        for (var generator : List.of(shuffle, replace)) {
            generator.fragmentFactory = factory;
            generator.formulaCalculator = calculator;
        }
        shuffle.libraryTsvParser = parser;
        replace.libraryTsvParser = parser;
    }

    private static PeptideDO peptide(String sequence, String... annotations) {
        PeptideDO peptide = new PeptideDO();
        peptide.setSequence(sequence);
        peptide.setFullName(sequence);
        for (String annotation : annotations) {
            FragmentInfo fi = new FragmentInfo();
            fi.setAnnotations(annotation);
            fi.setCutInfo(annotation);
            fi.setCharge(1);
            fi.setIntensity(10d);
            peptide.getFragments().add(fi);
        }
        return peptide;
    }

    private static String sorted(String s) {
        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    private static void assertNoDecoy(PeptideDO peptide) {
        assertNull(peptide.getDecoySequence());
        assertTrue(peptide.getDecoyFragments().isEmpty());
    }

    @Test
    void shuffleKeepsCompositionAndFixedCTerminus() {
        PeptideDO peptide = peptide("ACDEFGHIK", "b3", "y2");
        shuffle.generate(peptide);

        String decoy = peptide.getDecoySequence();
        assertEquals(peptide.getSequence().length(), decoy.length());
        assertEquals(sorted(peptide.getSequence()), sorted(decoy));
        assertEquals('K', decoy.charAt(decoy.length() - 1));
    }

    @Test
    void shuffleBuildsOneDecoyFragmentPerTargetFragment() {
        PeptideDO peptide = peptide("ACDEFGHIK", "b3", "y2");
        shuffle.generate(peptide);

        assertEquals(2, peptide.getDecoyFragments().size());
        FragmentInfo decoy = peptide.getDecoyFragments().get(1);
        assertEquals("y2", decoy.getCutInfo());
        assertEquals(10d, decoy.getIntensity());
        assertEquals(MOCK_MZ, decoy.getMz());
    }

    @Test
    void shuffleMovesModificationsWithTheirResidue() {
        PeptideDO peptide = peptide("ACDEFGHIK", "b3");
        HashMap<Integer, String> mods = new HashMap<>();
        mods.put(1, "4");
        peptide.setUnimodMap(mods);
        shuffle.generate(peptide);

        int cysteine = peptide.getDecoySequence().indexOf('C');
        assertEquals(1, peptide.getDecoyUnimodMap().size());
        assertEquals("4", peptide.getDecoyUnimodMap().get(cysteine));
    }

    @Test
    void shuffleHandlesPeptideWithOnlyTheFixedResidue() {
        PeptideDO peptide = peptide("K", "y1");
        shuffle.generate(peptide);
        assertEquals("K", peptide.getDecoySequence());
        assertEquals(1, peptide.getDecoyFragments().size());
    }

    @Test
    void shuffleLeavesNoDecoyWhenFragmentLocationIsOutOfRange() {
        PeptideDO peptide = peptide("ACDEFGHIK", "b3", "y20");
        assertDoesNotThrow(() -> shuffle.generate(peptide));
        assertNoDecoy(peptide);
    }

    @Test
    void shuffleLeavesNoDecoyWhenIonTypeIsUnknown() {
        PeptideDO peptide = peptide("ACDEFGHIK", "q3");
        assertDoesNotThrow(() -> shuffle.generate(peptide));
        assertNoDecoy(peptide);
    }

    @Test
    void generatingAListResetsPreviousDecoys() {
        PeptideDO peptide = peptide("ACDEFGHIK", "b3");
        shuffle.generate(peptide);
        shuffle.generate(new ArrayList<>(List.of(peptide)));
        assertEquals(1, peptide.getDecoyFragments().size());
    }

    @Test
    void replaceSwapsOnlyTheTerminalResidues() {
        PeptideDO peptide = peptide("GAVK", "b2", "y2");
        replace.generate(peptide);

        //G->L at the N terminus, K->L at the C terminus
        assertEquals("LAVL", peptide.getDecoySequence());
        assertEquals(2, peptide.getDecoyFragments().size());
    }

    @Test
    void replaceKeepsResiduesWithoutAReplacement() {
        PeptideDO peptide = peptide("XAVU", "b2");
        assertDoesNotThrow(() -> replace.generate(peptide));
        assertEquals("XAVU", peptide.getDecoySequence());
    }

    @Test
    void replaceLeavesNoPartialDecoyWhenALaterFragmentFails() {
        PeptideDO peptide = peptide("GAVK", "b2", "y20");
        assertDoesNotThrow(() -> replace.generate(peptide));
        assertNoDecoy(peptide);
    }
}
