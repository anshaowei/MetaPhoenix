package net.csibio.mslibrary.core.export;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.algorithm.entropy.Entropy;
import net.csibio.mslibrary.client.constants.enums.DecoyStrategy;
import net.csibio.mslibrary.client.constants.enums.SpectrumMatchMethod;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.LibraryHitService;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import net.csibio.mslibrary.core.config.VMProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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
    @Autowired
    LibraryHitService libraryHitService;
    @Autowired
    MongoTemplate mongoTemplate;

    public void scoreGraph(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO methodDO, int scoreInterval) {
        String fileName = "scoreGraph";
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export {} to {}", fileName, outputFileName);

        //header
        List<Object> header = Arrays.asList("BeginScore", "EndScore", "TargetFrequency", "DecoyFrequency", "TotalFrequency", "TTDC_FDR", "CTDC_FDR",
                "true_FDR", "BestSTDS_FDR", "STDS_FDR", "standard_FDR", "pValue", "PIT",
                "truePositive", "falsePositive", "trueNegative", "falseNegative", "FPR", "TPR", "AUC");
        List<List<Object>> dataSheet = getDataSheet(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO, scoreInterval, 1);
        dataSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet(fileName).doWrite(dataSheet);
        log.info("export {} success", fileName);
    }

    public void compareSpectrumMatchMethods(String queryLibraryId, String targetLibraryId, MethodDO methodDO, int scoreInterval) {
        String fileName = "SpectrumMatchMethodComparison";
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export {} to {}", fileName, outputFileName);

        //init
        List<List<Object>> compareSheet = new ArrayList<>();
        for (int i = 0; i < scoreInterval; i++) {
            List<Object> row = new ArrayList<>();
            compareSheet.add(row);
        }
        List<Object> header = new ArrayList<>();
        for (SpectrumMatchMethod spectrumMatchMethod : SpectrumMatchMethod.values()) {
            header.add(spectrumMatchMethod.getName());
            methodDO.setSpectrumMatchMethod(spectrumMatchMethod);
            List<List<Object>> dataSheet = getDataSheet(queryLibraryId, targetLibraryId, null, methodDO, scoreInterval, 1);
            for (int j = 0; j < dataSheet.size(); j++) {
                //trueFDR
                Double trueFDR = (Double) dataSheet.get(j).get(7);
                compareSheet.get(j).add(trueFDR);
            }
        }
        compareSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet(fileName).doWrite(compareSheet);
        log.info("export {} success", fileName);
    }

    public void compareDecoyStrategy(String queryLibraryId, String targetLibraryId, MethodDO methodDO, int scoreInterval) {
        String fileName = "DecoyStrategyComparison";
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export {} to {}", fileName, outputFileName);

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
        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
            String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + decoyStrategy.getName();
            List<List<Object>> dataSheet = getDataSheet(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO, scoreInterval, 1);
            header.add(decoyStrategy.getName());
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
                compareSheet.get(j).add(stdsFDR);
            }
            first = false;
        }
        compareSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet(fileName).doWrite(compareSheet);
        log.info("export {} success", fileName);
    }

    private List<List<Object>> getDataSheet(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO methodDO, int scoreInterval, int decoyMultiplier) {
        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = libraryHitService.getTargetDecoyHitsMap(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO);
        List<List<Object>> dataSheet = new ArrayList<>();

        //all hits above a score threshold for the target-decoy strategy
        List<LibraryHit> decoyHits = new ArrayList<>();
        List<LibraryHit> targetHits = new ArrayList<>();

        //the top score hits of each spectrum
        List<LibraryHit> bestDecoyHits = new ArrayList<>();
        List<LibraryHit> bestTargetHits = new ArrayList<>();
        List<LibraryHit> ctdcList = new ArrayList<>();

        //for true FDR calculation and ROC curve calculation
        List<LibraryHit> truePositives = new ArrayList<>();
        List<LibraryHit> falsePositives = new ArrayList<>();
        List<LibraryHit> tureNegatives = new ArrayList<>();
        List<LibraryHit> falseNegatives = new ArrayList<>();

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
                if (targetHitsList != null && targetHitsList.size() != 0) {
                    targetHitsList.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                    bestTargetHits.add(targetHitsList.get(0));
                    for (LibraryHit hit : targetHitsList) {
                        String[] inChIKeyArray = hit.getInChIKey().split("-");
                        if (inChIKeyArray[0].equals(k.getInChIKey().split("-")[0])) {
                            truePositives.add(hit);
                            falseNegatives.add(hit);
                            hit.setRight(true);
                        } else {
                            falsePositives.add(hit);
                            tureNegatives.add(hit);
                        }
                    }
                    targetHits.addAll(targetHitsList);
                }
                if (decoyHitsList != null && decoyHitsList.size() != 0) {
                    decoyHitsList.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                    bestDecoyHits.add(decoyHitsList.get(0));
                    decoyHits.addAll(decoyHitsList);
                }
            }
        });

        //AUC calculation
        double AUC = 0d, rightCount = 0d, falseCount = 0d;
        targetHits.sort(Comparator.comparing(LibraryHit::getScore));
        for (int i = 0; i < targetHits.size(); i++) {
            LibraryHit hit = targetHits.get(i);
            if (hit.isRight()) {
                AUC += i;
                rightCount++;
            } else {
                falseCount++;
            }
        }
        AUC = AUC - rightCount * (rightCount + 1) / 2;
        AUC = AUC / (rightCount * falseCount);

        //score range and step
        double minScore = 0.0;
        double maxScore = 1.0;
        double step = (maxScore - minScore) / scoreInterval;

        //estimate PIT
        double threshold = 0.5;
        double PIT = 0d;
        if (decoyHits.size() != 0) {
            PIT = (double) targetHits.stream().filter(hit -> hit.getScore() < threshold).toList().size() / decoyHits.stream().filter(hit -> hit.getScore() < threshold).toList().size() / decoyMultiplier;
        }

        for (int i = 0; i < scoreInterval; i++) {
            double finalMinScore = minScore + i * step;
            double finalMaxScore = minScore + (i + 1) * step;
            List<Object> row = new ArrayList<>();

            //concatenated target-decoy strategy calculation
            int target;
            double decoy;
            List<LibraryHit> hitsAboveScore = ctdcList.stream().filter(hit -> hit.getScore() > finalMinScore).toList();
            target = hitsAboveScore.stream().filter(hit -> !hit.isDecoy()).toList().size();
            decoy = (double) hitsAboveScore.stream().filter(LibraryHit::isDecoy).toList().size() / decoyMultiplier;
            double CTDC_FDR = (target + decoy == 0) ? 0d : (double) 2 * decoy / (target + decoy);
            double TTDC_FDR = (target == 0) ? 0d : decoy / target;

            //separated target-decoy strategy calculation
            int targetCount, bestTargetCount;
            int truePositiveCount, falsePositiveCount, trueNegativeCount, falseNegativeCount;
            double decoyCount, bestDecoyCount;
            targetCount = targetHits.stream().filter(hit -> hit.getScore() >= finalMinScore).toList().size();
            decoyCount = (double) decoyHits.stream().filter(hit -> hit.getScore() >= finalMinScore).toList().size() / decoyMultiplier;
            bestTargetCount = bestTargetHits.stream().filter(hit -> hit.getScore() >= finalMinScore).toList().size();
            bestDecoyCount = (double) bestDecoyHits.stream().filter(hit -> hit.getScore() >= finalMinScore).toList().size() / decoyMultiplier;

            //real data calculation
            truePositiveCount = truePositives.stream().filter(hit -> hit.getScore() >= finalMinScore).toList().size();
            falsePositiveCount = falsePositives.stream().filter(hit -> hit.getScore() >= finalMinScore).toList().size();
            trueNegativeCount = tureNegatives.stream().filter(hit -> hit.getScore() < finalMinScore).toList().size();
            falseNegativeCount = falseNegatives.stream().filter(hit -> hit.getScore() < finalMinScore).toList().size();
            double trueFDR = 0d, BestSTDS_FDR = 0d, STDS_FDR = 0d, pValue = 0d;
            if (truePositiveCount + falsePositiveCount != 0) {
                trueFDR = (double) falsePositiveCount / (truePositiveCount + falsePositiveCount);
            }
            if (bestTargetCount != 0) {
                BestSTDS_FDR = bestDecoyCount / (bestTargetCount + bestDecoyCount);
            }
            if (targetCount != 0) {
                STDS_FDR = decoyCount / (targetCount + decoyCount);
                pValue = decoyCount / (targetCount);
            }

            //hits distribution
            if (i == 0) {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = (double) decoyHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size() / decoyMultiplier;
            } else {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = (double) decoyHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size() / decoyMultiplier;
            }

            //write data sheet
            //start score
            row.add(finalMinScore);
            //end score
            row.add(finalMaxScore);
            //target frequency
            row.add((double) targetCount / targetHits.size());
            //decoy frequency
            row.add(decoyHits.size() == 0 ? 0 : decoyCount / decoyHits.size());
            //total frequency
            row.add((targetCount + decoyCount) / (targetHits.size() + decoyHits.size()));
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
            //true positive count
            row.add(truePositiveCount);
            //false positive count
            row.add(falsePositiveCount);
            //true negative count
            row.add(trueNegativeCount);
            //false negative count
            row.add(falseNegativeCount);
            //false positive rate
            if (falsePositiveCount + trueNegativeCount != 0)
                row.add(falsePositiveCount / (double) (falsePositiveCount + trueNegativeCount));
            //ture positive rate
            if (truePositiveCount + falseNegativeCount != 0)
                row.add(truePositiveCount / (double) (truePositiveCount + falseNegativeCount));
            //AUC
            row.add(AUC);
            dataSheet.add(row);
        }
        return dataSheet;
    }

    public void estimatedPValueGraph(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO methodDO, int pInterval) {
        String fileName = "estimatedPValueGraph";
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export estimatedPValue graph : " + outputFileName);
        List<List<Object>> dataSheet = getDataSheet(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO, 1000, 1);

        //reverse score data sheet to make pValue ascending
        Collections.reverse(dataSheet);
        List<List<Object>> resultDatasheet = new ArrayList<>();
        List<Double> thresholds = new ArrayList<>();

        //pValue thresholds
        double step = 1.0 / pInterval;
        for (int i = 0; i < pInterval; i++) {
            thresholds.add(step * (i + 1));
        }

        //record real pValue and frequencies from pValue 0~1
        double[] pValueArray = new double[dataSheet.size()];
        List<Integer> turePositiveList = new ArrayList<>();
        List<Integer> falsePositiveList = new ArrayList<>();
        for (int i = 0; i < dataSheet.size(); i++) {
            pValueArray[i] = (double) dataSheet.get(i).get(11);
            Integer turePositive = (Integer) dataSheet.get(i).get(13);
            Integer falsePositive = (Integer) dataSheet.get(i).get(14);
            turePositiveList.add(turePositive);
            falsePositiveList.add(falsePositive);
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

        //calculate the frequency of each threshold according to the index
        for (int i = 0; i < indexList.size(); i++) {
            int index = indexList.get(i);
            List<Object> row = new ArrayList<>();
            row.add(thresholds.get(i));
            row.add(dataSheet.get(index).get(11));
            if (i == 0) {
                row.add((double) turePositiveList.get(index));
                row.add((double) falsePositiveList.get(index));
            } else if (i == indexList.size() - 1) {
                row.add(turePositiveList.get(turePositiveList.size() - 1) - turePositiveList.get(index));
                row.add(falsePositiveList.get(falsePositiveList.size() - 1) - falsePositiveList.get(index));
            } else {
                row.add(turePositiveList.get(index) - turePositiveList.get(indexList.get(i - 1)));
                row.add(falsePositiveList.get(index) - falsePositiveList.get(indexList.get(i - 1)));
            }
            resultDatasheet.add(row);
        }

        //header
        List<Object> header = Arrays.asList("EstimatedPValue", "RealPValue", "True", "False");
        resultDatasheet.add(0, header);

        EasyExcel.write(outputFileName).sheet("estimatedPValueGraph").doWrite(resultDatasheet);
        log.info("export estimatedPValue graph success: " + outputFileName);
    }

    public void entropyDistributionGraph(String libraryId, int interval) {
        String fileName = "entropyDistributionGraph";
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("Start export {} to {}", fileName, outputFileName);

        Set<String> names = mongoTemplate.getCollectionNames();
        HashMap<String, List<SpectrumDO>> idSpectraMap = new HashMap<>();
        for (String name : names) {
            if (!name.contains(libraryId)) {
                continue;
            }
            List<SpectrumDO> spectrumDOS = mongoTemplate.findAll(SpectrumDO.class, name);
            if (spectrumDOS.size() == 0) {
                continue;
            }
            if (name.equals("spectrum" + SymbolConst.DELIMITER + libraryId)) {
                idSpectraMap.put("raw", spectrumDOS);
            } else {
                name = name.replace("spectrum-" + libraryId + SymbolConst.DELIMITER, "");
                idSpectraMap.put(name, spectrumDOS);
            }
        }

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
                        entropyList.add(Entropy.getSpectrumEntropy(spectrumDO.getSpectrum()));
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
        log.info("export {} success", fileName);
    }

    public void simpleScoreGraph(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO methodDO, int scoreInterval, boolean bestHit, boolean logScale, int minLogScore) {
        String fileName = "simpleScoreGraph";
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("Start export {} to {}", fileName, outputFileName);

        //header
        List<Object> header = Arrays.asList("BeginScore", "EndScore", "Target", "Decoy");
        List<List<Object>> dataSheet = getSimpleDataSheet(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO, scoreInterval, bestHit, logScale, minLogScore);
        dataSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet("scoreGraph").doWrite(dataSheet);
        log.info("export {} success", fileName);
    }

    private List<List<Object>> getSimpleDataSheet(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO methodDO, int scoreInterval, boolean bestHit, boolean logScale, int minLogScore) {
        List<SpectrumDO> querySpectrumDOS = spectrumService.getAllByLibraryId(queryLibraryId);
        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = libraryHitService.getTargetDecoyHitsMap(querySpectrumDOS, targetLibraryId, decoyLibraryId, methodDO);
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
