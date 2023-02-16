package net.csibio.mslibrary.core.export;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.algorithm.search.FDRControlled;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import net.csibio.mslibrary.core.config.VMProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    FDRControlled fdrControlled;

    public Result toExcel(String fileName, List<LibraryHit> libraryHits) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        EasyExcel.write(outputFileName, LibraryHit.class).sheet("result").doWrite(libraryHits);
        return null;
    }

    public Result toMsp(String fileName, String libraryId) {
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        if (spectrumDOS.size() == 0) {
            return Result.Error("no spectra found in {}", libraryId);
        }
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".msp";
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(outputFileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (SpectrumDO spectrumDO : spectrumDOS) {
                if (spectrumDO.getCompoundName() != null) {
                    bufferedWriter.write("NAME: " + spectrumDO.getCompoundName());
                    bufferedWriter.newLine();
                }
                //precursor m/z
                if (spectrumDO.getPrecursorMz() != null) {
                    bufferedWriter.write("PRECURSORMZ: " + spectrumDO.getPrecursorMz());
                    bufferedWriter.newLine();
                }
                //precursor type
                if (spectrumDO.getPrecursorAdduct() != null) {
                    bufferedWriter.write("PRECURSORTYPE: " + spectrumDO.getPrecursorAdduct());
                    bufferedWriter.newLine();
                }
                //formula
                if (spectrumDO.getFormula() != null) {
                    bufferedWriter.write("FORMULA: " + spectrumDO.getFormula());
                    bufferedWriter.newLine();
                }
                //inchiKey
                if (spectrumDO.getInChIKey() != null) {
                    bufferedWriter.write("INCHIKEY: " + spectrumDO.getInChIKey());
                    bufferedWriter.newLine();
                }
                //inchi
                if (spectrumDO.getInChI() != null) {
                    bufferedWriter.write("INCHI: " + spectrumDO.getInChI());
                    bufferedWriter.newLine();
                }
                //smiles
                if (spectrumDO.getSmiles() != null) {
                    bufferedWriter.write("SMILES: " + spectrumDO.getSmiles());
                    bufferedWriter.newLine();
                }
                //ionmode
                if (spectrumDO.getIonMode() != null) {
                    bufferedWriter.write("IONMODE: " + spectrumDO.getIonMode());
                    bufferedWriter.newLine();
                }
                //instrumentType
                if (spectrumDO.getInstrumentType() != null) {
                    bufferedWriter.write("INSTRUMENTTYPE: " + spectrumDO.getInstrumentType());
                    bufferedWriter.newLine();
                }
                //instrument
                if (spectrumDO.getInstrument() != null) {
                    bufferedWriter.write("INSTRUMENT: " + spectrumDO.getInstrument());
                    bufferedWriter.newLine();
                }
                //collisionEnergy
                if (spectrumDO.getCollisionEnergy() != null) {
                    bufferedWriter.write("COLLISIONENERGY: " + spectrumDO.getCollisionEnergy());
                    bufferedWriter.newLine();
                }
                //comment
                if (spectrumDO.getComment() != null) {
                    bufferedWriter.write("COMMENT: " + spectrumDO.getComment());
                    bufferedWriter.newLine();
                }
                //mz and ints
                if (spectrumDO.getMzs() != null && spectrumDO.getInts() != null) {
                    bufferedWriter.write("Num Peaks: " + spectrumDO.getMzs().length);
                    bufferedWriter.newLine();
                    for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                        bufferedWriter.write(spectrumDO.getMzs()[i] + " " + spectrumDO.getInts()[i]);
                        bufferedWriter.newLine();
                    }
                }
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("export msp file success : " + outputFileName);
        return new Result(true);
    }

    public Result toMgf(String fileName, String libraryId) {
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        if (spectrumDOS.size() == 0) {
            return Result.Error("no spectra found in {}", libraryId);
        }
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".mgf";
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(outputFileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (SpectrumDO spectrumDO : spectrumDOS) {
                bufferedWriter.write("BEGIN IONS");
                bufferedWriter.newLine();
                //FEATURE_ID
                if (spectrumDO.getId() != null) {
                    bufferedWriter.write("FEATURE_ID: " + spectrumDO.getId());
                    bufferedWriter.newLine();
                }
                //PEPMASS
                if (spectrumDO.getPrecursorMz() != null) {
                    bufferedWriter.write("PEPMASS: " + spectrumDO.getPrecursorMz());
                    bufferedWriter.newLine();
                }
                //CHARGE
                bufferedWriter.write("CHARGE: " + "1");
                bufferedWriter.newLine();
                //MSLEVEL
                bufferedWriter.write("MSLEVEL: " + "2");
                bufferedWriter.newLine();
                if (spectrumDO.getMzs() != null && spectrumDO.getInts() != null) {
                    for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                        bufferedWriter.write(spectrumDO.getMzs()[i] + " " + spectrumDO.getInts()[i]);
                        bufferedWriter.newLine();
                    }
                }
                bufferedWriter.write("END IONS");
                bufferedWriter.newLine();
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("export msp file success : " + outputFileName);
        return new Result(true);
    }

    public void scoreGraph(String fileName, ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export score graph : " + outputFileName);
        //header
        List<Object> header = Arrays.asList("BeginScore", "EndScore", "Target", "Decoy", "Total", "FDR",
                "TrueFDR", "PValue", "PIT", "TargetNum", "DecoyNum", "TotalNum", "trueNum", "falseNum");
        List<List<Object>> dataSheet = getDataSheet(hitsMap, scoreInterval);
        dataSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet("scoreGraph").doWrite(dataSheet);
        log.info("export score graph success : " + outputFileName);
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
                    row.add(targetFrequencyList.get(index) - targetFrequencyList.get(indexList.get(i - 1)));
                    row.add(decoyFrequencyList.get(index) - decoyFrequencyList.get(indexList.get(i - 1)));
                    row.add(totalFrequencyList.get(index) - totalFrequencyList.get(indexList.get(i - 1)));
                }
            }
            dataSheet.add(row);
        }

        EasyExcel.write(outputFileName).sheet("estimatedPValueGraph").doWrite(dataSheet);
        log.info("export estimatedPValue graph success : " + outputFileName);
    }

    private List<List<Object>> getDataSheet(ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval) {
        List<List<Object>> dataSheet = new ArrayList<>();
        //for target-decoy FDR calculation
        //all hits in the target library above score threshold
        List<LibraryHit> allTargetHits = new ArrayList<>();
        //top score hit for a spectrum in the decoy library
        List<LibraryHit> decoyHits = new ArrayList<>();
        //top score hit for a spectrum in the target library
        List<LibraryHit> targetHits = new ArrayList<>();

        //for true FDR calculation
        //the top score hit in the target library and has the same smiles with the query spectrum
        List<LibraryHit> trueHits = new ArrayList<>();
        //the hits in the target library above the given score threshold but not with the same smiles
        List<LibraryHit> falseHits = new ArrayList<>();

        hitsMap.forEach((k, v) -> {
            if (v.size() != 0) {
                Map<Boolean, List<LibraryHit>> targetDecoyMap = v.stream().collect(Collectors.groupingBy(LibraryHit::isDecoy));
                for (Map.Entry<Boolean, List<LibraryHit>> entry : targetDecoyMap.entrySet()) {
                    if (entry.getKey()) {
                        targetDecoyMap.get(true).sort(Comparator.comparing(LibraryHit::getScore).reversed());
                        decoyHits.add(targetDecoyMap.get(true).get(0));
                    } else {
                        targetDecoyMap.get(false).sort(Comparator.comparing(LibraryHit::getScore).reversed());
                        List<LibraryHit> temp = new ArrayList<>();
                        for (LibraryHit hit : targetDecoyMap.get(false)) {
                            if (hit.getSmiles().equals(k.getSmiles())) {
                                temp.add(hit);
                            } else {
                                falseHits.add(hit);
                            }
                        }
                        if (temp.size() != 0) {
                            trueHits.add(temp.get(0));
                        }
                        allTargetHits.addAll(targetDecoyMap.get(false));
                        targetHits.add(targetDecoyMap.get(false).get(0));
                    }
                }
            }
        });

        //choose the range as given or calculated
//        double minScore = Math.min(decoyHits.get(0).getScore(), targetHits.get(0).getScore());
//        double maxScore = Math.max(decoyHits.get(decoyHits.size() - 1).getScore(), targetHits.get(targetHits.size() - 1).getScore());
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
            double pit = (double) incorrectCount / (targetCount + incorrectCount);
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
}
