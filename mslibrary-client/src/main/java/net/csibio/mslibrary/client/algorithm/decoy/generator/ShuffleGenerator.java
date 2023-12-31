package net.csibio.mslibrary.client.algorithm.decoy.generator;

import net.csibio.mslibrary.client.algorithm.decoy.BaseGenerator;
import net.csibio.mslibrary.client.constants.Constants;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 一种常规的伪肽段生成算法,控制每一个肽段的最后一位不变,前面的氨基酸进行随机打乱,循环10次,选取其中重复度最低的一次作为最后的生成结果
 * <p>
 * Created by James Lu MiaoShan
 * Time: 2018-06-07 10:22
 */
@Component("shuffleGenerator")
public class ShuffleGenerator extends BaseGenerator {

    public final Logger logger = LoggerFactory.getLogger(ShuffleGenerator.class);

    public static final String NAME = "shuffle";

    @Autowired
    LibraryTsvParser libraryTsvParser;

    @Override
    public void generate(PeptideDO peptideDO) {

        String sequence = peptideDO.getSequence();
        HashMap<Integer, String> unimodMap = peptideDO.getUnimodMap();

        List<AminoAcid> aminoAcids = null;

        //最后一位是K,P,R时保持最后一位氨基酸位置不变
        char lastAcidChar = sequence.toUpperCase().charAt(sequence.length() - 1);
        AminoAcid lastAcid = null;

        boolean removeLastAcid = false;
        if (lastAcidChar == 'K' || lastAcidChar == 'P' || lastAcidChar == 'R') {
            removeLastAcid = true;
            lastAcid = new AminoAcid();
            sequence = sequence.substring(0, sequence.length() - 1);
            lastAcid.setName(String.valueOf(lastAcidChar));
            if (unimodMap != null && unimodMap.get(sequence.length()) != null) {
                lastAcid.setModId(unimodMap.get(sequence.length()));
            }
        }

        aminoAcids = fragmentFactory.parseAminoAcid(sequence, unimodMap);

        List<AminoAcid> bestDecoy = null;
        Double asi = null;
        HashMap<Integer, String> newUnimodMap = new HashMap<>();

        //生成十个随机打乱的数组,比对重复度
        for (int i = 0; i < Constants.DECOY_GENERATOR_TRY_TIMES; i++) {

            Collections.shuffle(aminoAcids);

            String newSequence = TransitionUtil.toSequence(aminoAcids, false);
            double tempAsi = aaSequenceIdentify(sequence, newSequence);
            if (asi == null || asi > tempAsi) {
                asi = tempAsi;
                bestDecoy = aminoAcids;
                //如果已经生成一个重复度为0的肽段则可以直接跳出循环
                if (asi == 0) {
                    break;
                }
            }
            aminoAcids = fragmentFactory.parseAminoAcid(sequence, unimodMap);
        }

        if (removeLastAcid) {
            bestDecoy.add(lastAcid);
        }

        for (int i = 0; i < bestDecoy.size(); i++) {
            if (bestDecoy.get(i).getModId() != null) {
                newUnimodMap.put(i, bestDecoy.get(i).getModId());
            }
        }

        for (FragmentInfo targetFi : peptideDO.getFragments()) {
            FragmentInfo decoyFi = new FragmentInfo();
            decoyFi.setCutInfo(targetFi.getCutInfo());
            decoyFi.setIntensity(targetFi.getIntensity());
            decoyFi.setCharge(targetFi.getCharge());
            decoyFi.setAnnotations(targetFi.getAnnotations());
            Annotation oneAnno = libraryTsvParser.parseAnnotation(targetFi.getAnnotations());
            List<String> unimodIds = new ArrayList<>();
            List<AminoAcid> acids = null;
            try {
                acids = fragmentFactory.getFragmentSequence(bestDecoy, oneAnno.getType(), oneAnno.getLocation());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(peptideDO.getFullName());
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
            peptideDO.getDecoyFragments().add(decoyFi);
        }

        peptideDO.setDecoySequence(TransitionUtil.toSequence(bestDecoy, false));
        peptideDO.setDecoyUnimodMap(newUnimodMap);
    }
}
