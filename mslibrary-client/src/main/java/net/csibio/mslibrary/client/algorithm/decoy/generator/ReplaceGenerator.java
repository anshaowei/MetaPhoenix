package net.csibio.mslibrary.client.algorithm.decoy.generator;

import net.csibio.mslibrary.client.algorithm.decoy.BaseGenerator;
import net.csibio.mslibrary.client.domain.bean.parser.model.chemistry.AminoAcid;
import net.csibio.mslibrary.client.domain.bean.peptide.Annotation;
import net.csibio.mslibrary.client.domain.bean.peptide.FragmentInfo;
import net.csibio.mslibrary.client.domain.db.PeptideDO;
import net.csibio.mslibrary.client.parser.dia.LibraryTsvParser;
import net.csibio.mslibrary.client.utils.TransitionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component("replaceGenerator")
public class ReplaceGenerator extends BaseGenerator {

    public final Logger logger = LoggerFactory.getLogger(ReplaceGenerator.class);

    public static final String NAME = "replace";

    @Autowired
    LibraryTsvParser libraryTsvParser;

    String origin = "GAVLIFMPWSCTYHKRQEND";
    String target = "LLLVVLLLLTSSSSLLNDQE";

    @Override
    protected void generate(PeptideDO peptide) {
        String sequence = peptide.getSequence();
        HashMap<Integer, String> unimodMap = peptide.getUnimodMap();
        List<AminoAcid> aminoAcids = fragmentFactory.parseAminoAcid(sequence, unimodMap);

        AminoAcid b = aminoAcids.get(0);
        AminoAcid y = aminoAcids.get(aminoAcids.size() - 1);

        b.setName(replacement(b.getName()));
        y.setName(replacement(y.getName()));

        for (FragmentInfo targetFi : peptide.getFragments()) {
            FragmentInfo decoyFi = new FragmentInfo();
            decoyFi.setCutInfo(targetFi.getCutInfo());
            decoyFi.setIntensity(targetFi.getIntensity());
            decoyFi.setCharge(targetFi.getCharge());
            decoyFi.setAnnotations(targetFi.getAnnotations());
            Annotation oneAnno = libraryTsvParser.parseAnnotation(targetFi.getAnnotations());
            List<String> unimodIds = new ArrayList<>();
            List<AminoAcid> acids = null;
            try {
                acids = fragmentFactory.getFragmentSequence(aminoAcids, oneAnno.getType(), oneAnno.getLocation());
            } catch (Exception e) {
                logger.error("Decoy generation failed for peptide {}, fragment annotation {}", peptide.getFullName(), targetFi.getAnnotations(), e);
            }
            if (acids == null) {
                //a partial decoy would bias the scores, so leave this peptide without any decoy
                logger.error("Skip decoy generation for peptide {}: unusable fragment annotation {}", peptide.getFullName(), targetFi.getAnnotations());
                peptide.clearDecoy();
                return;
            }
            for (AminoAcid aminoAcid : acids) {
                if (aminoAcid.getModId() != null) {
                    unimodIds.add(aminoAcid.getModId());
                }
            }

            double productMz = formulaCalculator.getMonoMz(
                    TransitionUtil.toSequence(acids, false),
                    oneAnno.getType(),
                    oneAnno.getCharge(),
                    oneAnno.getAdjust(),
                    oneAnno.getDeviation(),
                    oneAnno.isIsotope(),
                    unimodIds
            );

            decoyFi.setMz(productMz);
            peptide.getDecoyFragments().add(decoyFi);
        }

        peptide.setDecoySequence(TransitionUtil.toSequence(aminoAcids, false));
        peptide.setDecoyUnimodMap(unimodMap);
    }

    /**
     * Residues outside the 20 standard amino acids (e.g. X, U) have no replacement and are kept as they are.
     */
    private String replacement(String residue) {
        int index = origin.indexOf(residue);
        return index < 0 ? residue : String.valueOf(target.charAt(index));
    }
}
