package net.csibio.metaphoenix.client.algorithm.score;


import net.csibio.metaphoenix.client.constants.IsotopeConst;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("isotopeCalculator")
public class IsotopeCalculator {

    public Double[] getTheoDistribution(String formula, int maxIsotope) {
        if (formula == null) {
            return null;
        }
        String regex = "([A-Z])(\\d+)";
        Matcher matcher = Pattern.compile(regex).matcher(formula);
        HashMap<String, Integer> elementMap = new HashMap<>();
        while (matcher.find()) {
            elementMap.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
        }
        if (elementMap.size() <= 1) {
            return null;
        }
        //Theory Distribution
        List<Double[]> distributions = new ArrayList<>();
        Double[] isotopeDistributionC = convolvePow(IsotopeConst.C, elementMap.get("C"), maxIsotope);
        Double[] isotopeDistributionH = convolvePow(IsotopeConst.H, elementMap.get("H"), maxIsotope);
        Double[] isotopeDistributionN = convolvePow(IsotopeConst.N, elementMap.get("N"), maxIsotope);
        Double[] isotopeDistributionO = convolvePow(IsotopeConst.O, elementMap.get("O"), maxIsotope);
        Double[] isotopeDistributionS = convolvePow(IsotopeConst.S, elementMap.get("S"), maxIsotope);
        if (isotopeDistributionC != null) {
            distributions.add(isotopeDistributionC);
        }
        if (isotopeDistributionH != null) {
            distributions.add(isotopeDistributionH);
        }
        if (isotopeDistributionN != null) {
            distributions.add(isotopeDistributionN);
        }
        if (isotopeDistributionO != null) {
            distributions.add(isotopeDistributionO);
        }
        if (isotopeDistributionS != null) {
            distributions.add(isotopeDistributionS);
        }

        Double[] theoDistribution;
        theoDistribution = convolve(distributions.get(0), distributions.get(1), maxIsotope);
        for (int i = 2; i < distributions.size(); i++) {
            theoDistribution = convolve(theoDistribution, distributions.get(i), maxIsotope);
        }
        return theoDistribution;
    }

    /**
     * @param factor number of predicted element
     * @return
     */
    private Double[] convolvePow(List<Double[]> distribution, Integer factor, int maxIsotope) {
        if (factor == null) {
            return null;
        }
        if (factor == 1) {
            return distribution.get(0);
        }
        int log2n = (int) Math.ceil(FastMath.log(2, factor));

        Double[] distributionResult;
        if ((factor & 1) == 1) {
            distributionResult = distribution.get(0);
        } else {
            distributionResult = new Double[]{1d};
        }
        for (int i = 1; i <= log2n; i++) {
            if ((factor & (1 << i)) == 1 << i) {
                distributionResult = convolve(distributionResult, distribution.get(i), maxIsotope);
            }
        }
        return distributionResult;
    }

    private Double[] convolve(Double[] leftDistribution, Double[] rightFormerResult, int maxIsotope) {
        int rMax = leftDistribution.length + rightFormerResult.length - 1;
        if (maxIsotope != 0 && rMax > maxIsotope) {
            rMax = maxIsotope;
        }
        Double[] result = new Double[rMax];
        for (int i = 0; i < rMax; i++) {
            result[i] = 0d;
        }
        for (int i = leftDistribution.length - 1; i >= 0; i--) {
            for (int j = Math.min(rMax - i, rightFormerResult.length) - 1; j >= 0; j--) {
                result[i + j] += leftDistribution[i] * rightFormerResult[j];
            }
        }
        return result;
    }
}
