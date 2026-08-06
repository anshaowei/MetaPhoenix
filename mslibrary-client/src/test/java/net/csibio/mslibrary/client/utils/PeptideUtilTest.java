package net.csibio.mslibrary.client.utils;

import net.csibio.mslibrary.client.domain.db.PeptideDO;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeptideUtilTest {

    @Test
    void parseProteinSplitsSlashDelimitedLabelIntoProteinSet() {
        Set<String> proteins = PeptideUtil.parseProtein("5/sp|Q9NY65|TBA8_HUMAN/sp|Q6PEY2|TBA3E_HUMAN");
        assertEquals(Set.of("sp|Q9NY65|TBA8_HUMAN", "sp|Q6PEY2|TBA3E_HUMAN"), proteins);
    }

    @Test
    void parseProteinTreatsIrtLabelSpecially() {
        assertEquals(Set.of("iRT"), PeptideUtil.parseProtein("irt-peptide"));
    }

    @Test
    void parseProteinReturnsSingleEntrySetWhenNoSeparators() {
        assertEquals(Set.of("PROT1"), PeptideUtil.parseProtein("PROT1"));
    }

    @Test
    void parseProteinReturnsEmptySetForNullInput() {
        assertTrue(PeptideUtil.parseProtein(null).isEmpty());
    }

    @Test
    void removeUnimodStripsParenthesizedModificationTags() {
        assertEquals("PEPTIDE", PeptideUtil.removeUnimod("PEP(UniMod:1)TIDE"));
    }

    @Test
    void removeUnimodReturnsInputUnchangedWhenNoParentheses() {
        assertEquals("PEPTIDE", PeptideUtil.removeUnimod("PEPTIDE"));
    }

    @Test
    void parseModificationFromStringExtractsUnimodPositionsAndIds() {
        // match starts at the residue preceding "(unimod:", i.e. the 'p' at index 2 in "pep(unimod:4)tide"
        HashMap<Integer, String> mods = PeptideUtil.parseModification("pep(unimod:4)tide");
        assertEquals("4", mods.get(2));
    }

    @Test
    void parseModificationFromStringReturnsEmptyMapWhenNoModification() {
        assertTrue(PeptideUtil.parseModification("peptide").isEmpty());
    }

    @Test
    void parseChargeFromCutInfoReturnsOneWhenNoCaretPresent() {
        assertEquals(1, PeptideUtil.parseChargeFromCutInfo("y3"));
    }

    @Test
    void parseChargeFromCutInfoExtractsChargeAfterCaret() {
        assertEquals(2, PeptideUtil.parseChargeFromCutInfo("y3^2"));
    }

    @Test
    void parseChargeFromCutInfoHandlesBracketAndIsotopeMarker() {
        assertEquals(2, PeptideUtil.parseChargeFromCutInfo("iy3^2[+1]"));
    }

    @Test
    void similarReturnsTrueWhenFingerprintOverlapMeetsThreshold() {
        PeptideDO a = new PeptideDO();
        a.setFingerPrints(new HashSet<>(Set.of(1f, 2f, 3f, 4f, 5f, 6f)));
        PeptideDO b = new PeptideDO();
        b.setFingerPrints(new HashSet<>(Set.of(1f, 2f, 3f, 4f, 5f, 6f, 7f)));

        assertTrue(PeptideUtil.similar(a, b));
    }

    @Test
    void similarReturnsFalseWhenFingerprintOverlapBelowThreshold() {
        PeptideDO a = new PeptideDO();
        a.setFingerPrints(new HashSet<>(Set.of(1f, 2f, 3f)));
        PeptideDO b = new PeptideDO();
        b.setFingerPrints(new HashSet<>(Set.of(4f, 5f, 6f)));

        assertFalse(PeptideUtil.similar(a, b));
    }
}
