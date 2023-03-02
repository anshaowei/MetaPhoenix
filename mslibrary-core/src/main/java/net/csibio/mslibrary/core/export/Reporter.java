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

    public void scoreGraph(String fileName, ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval, boolean bestHit) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export score graph : " + outputFileName);
        //header
        List<Object> header = Arrays.asList("BeginScore", "EndScore", "Target", "Decoy", "Total", "FDR",
                "TrueFDR", "PValue", "PIT", "TargetNum", "DecoyNum", "TotalNum", "trueNum", "falseNum");
        List<List<Object>> dataSheet = getDataSheet(hitsMap, scoreInterval, bestHit);
        dataSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet("scoreGraph").doWrite(dataSheet);
        log.info("export score graph success : " + outputFileName);
    }

    public void estimatedPValueGraph(String fileName, ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int pInterval, boolean bestHit) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export estimatedPValue graph : " + outputFileName);
        List<List<Object>> scoreDataSheet = getDataSheet(hitsMap, 100 * pInterval, bestHit);

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
            pValueArray[i] = (double) scoreDataSheet.get(i).get(6);
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

    public void entropyDistributionGraph(String fileName, String libraryId, int interval) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export entropy distribution graph : " + outputFileName);
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<List<Object>> dataSheet = new ArrayList<>();
        if (spectrumDOS.size() != 0) {
            List<Double> entropyList = new ArrayList<>();
            for (SpectrumDO spectrumDO : spectrumDOS) {
                entropyList.add(Entropy.getEntropy(spectrumDO.getSpectrum()));
            }
            Collections.sort(entropyList);
            double minValue = entropyList.get(0);
            double maxValue = entropyList.get(entropyList.size() - 1);
            double step = (maxValue - minValue) / interval;
            for (int i = 0; i < interval; i++) {
                List<Object> data = new ArrayList<>();
                double minThreshold = minValue + i * step;
                double maxThreshold = minValue + (i + 1) * step;
                double fraction = (double) entropyList.stream().filter(entropy -> entropy > minThreshold && entropy <= maxThreshold).toList().size() / entropyList.size();
                data.add(minThreshold);
                data.add(maxThreshold);
                data.add(fraction);
                dataSheet.add(data);
            }
            EasyExcel.write(outputFileName).sheet("entropyDistributionGraph").doWrite(dataSheet);
            log.info("export entropyDistributionGraph graph success: " + outputFileName);
        } else {
            log.error("No spectra in library: {}", libraryId);
        }
    }

    private List<List<Object>> getDataSheet(ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval, boolean bestHit) {
        List<List<Object>> dataSheet = new ArrayList<>();
        //all hits in the target library above score threshold
        List<LibraryHit> allTargetHits = new ArrayList<>();

        //for target-decoy estimated FDR calculation
        List<LibraryHit> decoyHits = new ArrayList<>();
        List<LibraryHit> targetHits = new ArrayList<>();

        //for true FDR calculation
        List<LibraryHit> trueHits = new ArrayList<>();
        List<LibraryHit> falseHits = new ArrayList<>();

        hitsMap.forEach((k, v) -> {
            if (v.size() != 0) {
                Map<Boolean, List<LibraryHit>> decoyTargetMap = v.stream().collect(Collectors.groupingBy(LibraryHit::isDecoy));
                List<LibraryHit> targetHitsList = decoyTargetMap.get(false);
                List<LibraryHit> decoyHitsList = decoyTargetMap.get(true);
                if (targetHitsList.size() != 0) {
                    targetHitsList.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                    if (bestHit) {
                        targetHits.add(targetHitsList.get(0));
                        if (targetHitsList.get(0).getSmiles().equals(k.getSmiles())) {
                            trueHits.add(targetHitsList.get(0));
                        } else {
                            falseHits.add(targetHitsList.get(0));
                        }
                    } else {
                        targetHits.addAll(targetHitsList);
                        for (LibraryHit libraryHit : targetHitsList) {
                            if (libraryHit.getSmiles().equals(k.getSmiles())) {
                                trueHits.add(libraryHit);
                            } else {
                                falseHits.add(libraryHit);
                            }
                        }
                    }
                    allTargetHits.addAll(targetHitsList);
                }
                if (decoyHitsList != null && decoyHitsList.size() != 0) {
                    decoyHitsList.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                    if (bestHit) {
                        decoyHits.add(decoyHitsList.get(0));
                    } else {
                        decoyHits.addAll(decoyHitsList);
                    }
                }
            }
        });

        //choose the range as given or calculated
        double minScore = 0.0;
        double maxScore = 1.0;
        double step = (maxScore - minScore) / scoreInterval;

        for (int i = 0; i < scoreInterval; i++) {
            double finalMinScore = minScore + i * step;
            double finalMaxScore = minScore + (i + 1) * step;
            int targetCount, decoyCount, incorrectCount, rightCount, falseCount, allTargetCount;
            List<Object> row = new ArrayList<>();

            //target-decoy strategy calculation
            targetCount = targetHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            decoyCount = decoyHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            allTargetCount = allTargetHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            incorrectCount = allTargetCount - targetCount;
            final int finalTargetCount = targetCount;
            final int finalDecoyCount = decoyCount;

            //real data calculation
            rightCount = trueHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            falseCount = falseHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            double trueFdr = (double) falseCount / (rightCount + falseCount);

            //calculate FDR, pValue and PIT
            double pit;
            if (bestHit) {
                pit = (double) incorrectCount / (targetCount + incorrectCount);
            } else {
                pit = 1.0;
            }
            double fdr = (double) decoyCount / (targetCount + decoyCount) * pit;
            double pValue = (double) decoyCount / (targetCount);

            //calculate hits distribution
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
            //FDR
            row.add(fdr);
            //trueFDR
            row.add(trueFdr);
            //pValue
            row.add(pValue);
            //PIT
            row.add(pit);
            //target count above score threshold
            row.add(finalTargetCount);
            //decoy count above score threshold
            row.add(finalDecoyCount);
            //total count above score threshold
            row.add(finalTargetCount + finalDecoyCount);
            //true count
            row.add(rightCount);
            //false count
            row.add(falseCount);
            dataSheet.add(row);
        }
        return dataSheet;
    }

    public void simpleScoreGraph(String fileName, ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export fake identification graph : " + outputFileName);
        //header
        List<Object> header = Arrays.asList("BeginScore", "EndScore", "Target", "Decoy");
        List<List<Object>> dataSheet = getSimpleDataSheet(hitsMap, scoreInterval, true, false, -30);
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
