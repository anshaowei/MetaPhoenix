package net.csibio.mslibrary.core.export;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.algorithm.similarity.Entropy;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import net.csibio.mslibrary.core.config.VMProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component("reporter")
@Slf4j
public class Reporter {

    @Autowired
    VMProperties vmProperties;
    @Autowired
    SpectrumService spectrumService;

    public void scoreGraph(String fileName, ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export score graph : " + outputFileName);
        //header
        List<Object> header = Arrays.asList("BeginScore", "EndScore", "TargetFrequency", "DecoyFrequency", "TotalFrequency", "TTDC_FDR", "CTDC_FDR",
                "true_FDR", "BestSTDS_FDR", "STDS_FDR", "standard_FDR", "pValue", "PIT", "true_Count", "false_Count");
        List<List<Object>> dataSheet = getDataSheet(hitsMap, scoreInterval);
        dataSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet("scoreGraph").doWrite(dataSheet);
        log.info("export score graph success : " + outputFileName);
    }

    public void compareSpectrumMatchMethods(String fileName, List<ConcurrentHashMap<SpectrumDO, List<LibraryHit>>> hitsMapList, int scoreInterval) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export compareFDRGraph : " + outputFileName);

        //init
        List<List<Object>> compareSheet = new ArrayList<>();
        for (int i = 0; i < scoreInterval; i++) {
            List<Object> row = new ArrayList<>();
            compareSheet.add(row);
        }
        List<Object> header = Arrays.asList("Cosine", "Entropy", "Unweighted", "MetaPro");
        compareSheet.add(0, header);

        for (int i = 0; i < hitsMapList.size(); i++) {
            List<List<Object>> dataSheet = getDataSheet(hitsMapList.get(i), scoreInterval);
            for (int j = 1; j < dataSheet.size(); j++) {
                //trueFDR
                Double trueFDR = (Double) dataSheet.get(j).get(7);
                compareSheet.get(j).add(trueFDR);
            }
        }
        EasyExcel.write(outputFileName).sheet("compareFDRGraph").doWrite(compareSheet);
        log.info("export compare success : " + outputFileName);
    }

    public void compareDecoyStrategy(String fileName, HashMap<String, ConcurrentHashMap<SpectrumDO, List<LibraryHit>>> hitsMapMap, int scoreInterval) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export compareDecoyStrategy : " + outputFileName);

        //init
        List<List<Object>> compareSheet = new ArrayList<>();
        for (int i = 0; i < scoreInterval; i++) {
            List<Object> row = new ArrayList<>();
            compareSheet.add(row);
        }
        List<Object> header = new ArrayList<>();
        header.add("tureFDR");
        header.add("StandardFDR");

        boolean first = true;
        for (String decoyStrategy : hitsMapMap.keySet()) {
            List<List<Object>> dataSheet = getDataSheet(hitsMapMap.get(decoyStrategy), scoreInterval);
            header.add(decoyStrategy);
            for (int j = 0; j < dataSheet.size(); j++) {
                //trueFDR
                Double trueFDR = (Double) dataSheet.get(j).get(7);
                //bestSTDS_FDR
                Double bestSTDSFDR = (Double) dataSheet.get(j).get(8);
                //STDS_FDR
                Double stdsFDR = (Double) dataSheet.get(j).get(9);
                if (first) {
                    compareSheet.get(j).add(trueFDR);
                    compareSheet.get(j).add(trueFDR);
                }
                compareSheet.get(j).add(bestSTDSFDR);
            }
            first = false;
        }
        compareSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet("compareDecoyStrategy").doWrite(compareSheet);
        log.info("export compare success : " + outputFileName);
    }


    private List<List<Object>> getDataSheet(ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval) {
        List<List<Object>> dataSheet = new ArrayList<>();

        //all hits above a score threshold for the target-decoy strategy
        List<LibraryHit> decoyHits = new ArrayList<>();
        List<LibraryHit> targetHits = new ArrayList<>();

        //the top score hits of each spectrum
        List<LibraryHit> bestDecoyHits = new ArrayList<>();
        List<LibraryHit> bestTargetHits = new ArrayList<>();
        List<LibraryHit> ctdcList = new ArrayList<>();

        //for true FDR calculation
        List<LibraryHit> trueHits = new ArrayList<>();
        List<LibraryHit> falseHits = new ArrayList<>();

        //collect data
        hitsMap.forEach((k, v) -> {
            if (v.size() != 0) {
                //concatenated target-decoy competition
                v.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                ctdcList.add(v.get(0));

                //separated target-decoy competition
                Map<Boolean, List<LibraryHit>> decoyTargetMap = v.stream().collect(Collectors.groupingBy(LibraryHit::isDecoy));
                List<LibraryHit> targetHitsList = decoyTargetMap.get(false);
                List<LibraryHit> decoyHitsList = decoyTargetMap.get(true);
                if (targetHitsList.size() != 0) {
                    targetHitsList.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                    bestTargetHits.add(targetHitsList.get(0));
                    targetHits.addAll(targetHitsList);
                    for (LibraryHit hit : targetHitsList) {
                        String[] inChIKeyArray = hit.getInChIKey().split("-");
                        if (inChIKeyArray[0].equals(k.getInChIKey().split("-")[0])) {
                            trueHits.add(hit);
                        } else {
                            falseHits.add(hit);
                        }
                    }
                }
                if (decoyHitsList != null && decoyHitsList.size() != 0) {
                    decoyHitsList.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                    bestDecoyHits.add(decoyHitsList.get(0));
                    decoyHits.addAll(decoyHitsList);
                }
            }
        });

        //score range and step
        double minScore = 0.0;
        double maxScore = 1.0;
        double step = (maxScore - minScore) / scoreInterval;

        //estimate PIT
        double threshold = 0.5;
        double PIT = (double) targetHits.stream().filter(hit -> hit.getScore() < threshold).toList().size() / decoyHits.stream().filter(hit -> hit.getScore() < threshold).toList().size();

        for (int i = 0; i < scoreInterval; i++) {
            double finalMinScore = minScore + i * step;
            double finalMaxScore = minScore + (i + 1) * step;
            List<Object> row = new ArrayList<>();

            //concatenated target-decoy strategy calculation
            int target, decoy;
            List<LibraryHit> hitsAboveScore = ctdcList.stream().filter(hit -> hit.getScore() > finalMinScore).toList();
            target = hitsAboveScore.stream().filter(hit -> !hit.isDecoy()).toList().size();
            decoy = hitsAboveScore.stream().filter(LibraryHit::isDecoy).toList().size();
            double CTDC_FDR = (double) 2 * decoy / (target + decoy);
            double TTDC_FDR = (double) decoy / target;

            //separated target-decoy strategy calculation
            int targetCount, decoyCount, rightCount, falseCount, bestTargetCount, bestDecoyCount;
            targetCount = targetHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            decoyCount = decoyHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            bestTargetCount = bestTargetHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            bestDecoyCount = bestDecoyHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();

            //real data calculation
            rightCount = trueHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            falseCount = falseHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            double trueFDR = 0d, BestSTDS_FDR = 0d, STDS_FDR = 0d, pValue = 0d;
            if (rightCount + falseCount != 0) {
                trueFDR = (double) falseCount / (rightCount + falseCount);
            }
            if (bestTargetCount != 0) {
                BestSTDS_FDR = (double) bestDecoyCount / bestTargetCount * PIT;
            }
            if (targetCount != 0) {
                STDS_FDR = (double) decoyCount / targetCount * PIT;
                pValue = (double) decoyCount / (targetCount);
            }

            //hits distribution
            if (i == 0) {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = decoyHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
            } else {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = decoyHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
            }

            //write data sheet
            //start score
            row.add(finalMinScore);
            //end score
            row.add(finalMaxScore);
            //target frequency
            row.add((double) targetCount / targetHits.size());
            //decoy frequency
            row.add((double) decoyCount / decoyHits.size());
            //total frequency
            row.add((double) (targetCount + decoyCount) / (targetHits.size() + decoyHits.size()));
            //CTDC FDR
            row.add(CTDC_FDR);
            //TTDC FDR
            row.add(TTDC_FDR);
            //trueFDR
            row.add(trueFDR);
            //BestSTDS FDR
            row.add(BestSTDS_FDR);
            //STDS FDR
            row.add(STDS_FDR);
            //standard FDR
            row.add(trueFDR);
            //pValue
            row.add(pValue);
            //PIT
            row.add(PIT);
            //true count
            row.add(rightCount);
            //false count
            row.add(falseCount);
            dataSheet.add(row);
        }
        return dataSheet;
    }

    public void estimatedPValueGraph(String fileName, ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int pInterval) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export estimatedPValue graph : " + outputFileName);
        List<List<Object>> scoreDataSheet = getDataSheet(hitsMap, 100 * pInterval);

        //reverse score data sheet to make pValue ascending
        Collections.reverse(scoreDataSheet);
        List<List<Object>> dataSheet = new ArrayList<>();
        List<Double> thresholds = new ArrayList<>();

        //pValue thresholds
        double step = 1.0 / pInterval;
        for (int i = 0; i < pInterval; i++) {
            thresholds.add(step * (i + 1));
        }

        //record real pValue and sum frequencies from pValue 0~1
        double[] pValueArray = new double[scoreDataSheet.size()];
        double targetFrequency = 0.0;
        double decoyFrequency = 0.0;
        double totalFrequency = 0.0;
        List<Double> targetFrequencyList = new ArrayList<>();
        List<Double> decoyFrequencyList = new ArrayList<>();
        List<Double> totalFrequencyList = new ArrayList<>();
        for (int i = 0; i < scoreDataSheet.size(); i++) {
            pValueArray[i] = (double) scoreDataSheet.get(i).get(10);
            targetFrequency += (double) scoreDataSheet.get(i).get(2);
            decoyFrequency += (double) scoreDataSheet.get(i).get(3);
            totalFrequency += (double) scoreDataSheet.get(i).get(4);
            targetFrequencyList.add(targetFrequency);
            decoyFrequencyList.add(decoyFrequency);
            totalFrequencyList.add(totalFrequency);
        }

        //for each threshold, find the nearest pValue and record the index
        List<Integer> indexList = new ArrayList<>();
        for (double threshold : thresholds) {
            int index = ArrayUtil.findNearestIndex(pValueArray, threshold);
            double diff = Math.abs(pValueArray[index] - threshold);
            if (diff > step) {
                indexList.add(-1);
            } else {
                indexList.add(index);
            }
        }

        //header
        List<Object> header = Arrays.asList("EstimatedPValue", "RealPValue", "TargetFrequency", "DecoyFrequency", "TotalFrequency");
        dataSheet.add(header);
        //calculate the frequency of each threshold according to the index
        for (int i = 0; i < indexList.size(); i++) {
            int index = indexList.get(i);
            List<Object> row = new ArrayList<>();
            row.add(thresholds.get(i));
            if (index == -1) {
                row.add("NA");
                row.add("NA");
                row.add("NA");
                row.add("NA");
            } else {
                row.add(scoreDataSheet.get(index).get(6));
                if (i == 0) {
                    row.add(targetFrequencyList.get(index));
                    row.add(decoyFrequencyList.get(index));
                    row.add(totalFrequencyList.get(index));
                } else {
                    if (indexList.get(i - 1) == -1) {
                        row.add(targetFrequencyList.get(index));
                        row.add(decoyFrequencyList.get(index));
                        row.add(totalFrequencyList.get(index));
                    } else {
                        row.add(targetFrequencyList.get(index) - targetFrequencyList.get(indexList.get(i - 1)));
                        row.add(decoyFrequencyList.get(index) - decoyFrequencyList.get(indexList.get(i - 1)));
                        row.add(totalFrequencyList.get(index) - totalFrequencyList.get(indexList.get(i - 1)));
                    }
                }
            }
            dataSheet.add(row);
        }

        EasyExcel.write(outputFileName).sheet("estimatedPValueGraph").doWrite(dataSheet);
        log.info("export estimatedPValue graph success: " + outputFileName);
    }

    public void entropyDistributionGraph(String fileName, HashMap<String, List<SpectrumDO>> idSpectraMap, int interval) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export entropy distribution graph : " + outputFileName);

        //init
        List<List<Object>> dataSheet = new ArrayList<>();
        List<Object> header = new ArrayList<>();
        for (int i = 0; i < interval; i++) {
            dataSheet.add(new ArrayList<>());
        }
        header.add("Entropy");

        for (int i = 0; i < interval; i++) {
            boolean first = true;
            for (Map.Entry<String, List<SpectrumDO>> entry : idSpectraMap.entrySet()) {
                List<SpectrumDO> spectrumDOS = entry.getValue();
                if (spectrumDOS.size() != 0) {
                    List<Double> entropyList = new ArrayList<>();
                    for (SpectrumDO spectrumDO : spectrumDOS) {
                        entropyList.add(Entropy.getEntropy(spectrumDO.getSpectrum()));
                    }
                    Collections.sort(entropyList);
                    double minValue = 0d;
                    double maxValue = 10d;
                    double step = (maxValue - minValue) / interval;
                    double lowerBound = minValue + step * i;
                    double upperBound = minValue + step * (i + 1);
                    int count = 0;
                    for (double entropy : entropyList) {
                        if (entropy >= lowerBound && entropy < upperBound) {
                            count++;
                        }
                    }
                    if (i == 0) {
                        header.add(entry.getKey());
                    }
                    if (first) {
                        dataSheet.get(i).add(upperBound);
                        first = false;
                    }
                    double frequency = (double) count / spectrumDOS.size();
                    dataSheet.get(i).add(frequency);
                } else {
                    dataSheet.get(i).add(0d);
                }
            }
        }
        dataSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet("entropyDistributionGraph").doWrite(dataSheet);
        log.info("export entropy distribution graph success: " + outputFileName);
    }

    public void simpleScoreGraph(String fileName, ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval, boolean bestHit, boolean logScale, int minLogScore) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export fake identification graph : " + outputFileName);
        //header
        List<Object> header = Arrays.asList("BeginScore", "EndScore", "Target", "Decoy");
        List<List<Object>> dataSheet = getSimpleDataSheet(hitsMap, scoreInterval, bestHit, logScale, minLogScore);
        dataSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet("scoreGraph").doWrite(dataSheet);
        log.info("export simple identification graph success : " + outputFileName);
    }

    private List<List<Object>> getSimpleDataSheet(ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval, boolean bestHit, boolean logScale, int minLogScore) {
        List<List<Object>> dataSheet = new ArrayList<>();
        List<LibraryHit> decoyHits = new ArrayList<>();
        List<LibraryHit> targetHits = new ArrayList<>();
        hitsMap.forEach((k, v) -> {
            if (v.size() != 0) {
                Map<Boolean, List<LibraryHit>> decoyTargetMap = v.stream().collect(Collectors.groupingBy(LibraryHit::isDecoy));
                if (bestHit) {
                    //remain only the best hit
                    if (decoyTargetMap.get(true) != null && decoyTargetMap.get(true).size() != 0) {
                        decoyTargetMap.get(true).sort(Comparator.comparing(LibraryHit::getScore).reversed());
                        decoyHits.add(decoyTargetMap.get(true).get(0));
                    }
                    if (decoyTargetMap.get(false) != null && decoyTargetMap.get(false).size() != 0) {
                        decoyTargetMap.get(false).sort(Comparator.comparing(LibraryHit::getScore).reversed());
                        targetHits.add(decoyTargetMap.get(false).get(0));
                    }
                } else {
                    //remain all the hits
                    if (decoyTargetMap.get(true) != null) {
                        decoyHits.addAll(decoyTargetMap.get(true));
                    }
                    if (decoyTargetMap.get(false) != null) {
                        targetHits.addAll(decoyTargetMap.get(false));
                    }
                }
            }
        });

        //use score or log(score) as x-axis
        List<Double> thresholds = new ArrayList<>();
        if (logScale) {
            for (int i = 0; i < scoreInterval; i++) {
                thresholds.add(Math.pow(2, minLogScore + i * Math.abs(minLogScore) / (double) scoreInterval));
            }
        } else {
            for (int i = 0; i < scoreInterval; i++) {
                thresholds.add((double) i / scoreInterval);
            }
        }

        for (int i = 0; i < scoreInterval; i++) {
            double finalMinScore;
            if (i == 0) {
                finalMinScore = 0.0;
            } else {
                finalMinScore = thresholds.get(i);
            }
            double finalMaxScore;
            if (i == scoreInterval - 1) {
                finalMaxScore = 1.0;
            } else {
                finalMaxScore = thresholds.get(i + 1);
            }
            int targetCount, decoyCount;
            List<Object> row = new ArrayList<>();

            //calculate hits distribution
            if (i == 0) {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = decoyHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
            } else {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = decoyHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
            }

            //write data sheet
            if (logScale) {
                row.add(minLogScore + i * Math.abs(minLogScore) / (double) scoreInterval);
                row.add(minLogScore + (i + 1) * Math.abs(minLogScore) / (double) scoreInterval);
            } else {
                row.add(finalMinScore);
                row.add(finalMaxScore);
            }
            row.add((double) targetCount / targetHits.size());
            if (decoyCount != 0) {
                row.add((double) decoyCount / decoyHits.size());
            } else {
                row.add(0.0);
            }
            dataSheet.add(row);
        }
        return dataSheet;
    }
}
