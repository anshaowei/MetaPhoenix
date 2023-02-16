package net.csibio.mslibrary.core.export;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.algorithm.search.FDRControlled;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.SpectrumService;
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

    public void scoreGraph(String fileName, ConcurrentHashMap<String, List<LibraryHit>> hitsMap, int scoreInterval) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export score graph : " + outputFileName);
        //header
        List<Object> header = Arrays.asList("BeginScore", "EndScore", "Target", "Decoy", "Total", "FDR", "PValue", "PIT");
        List<List<Object>> dataSheet = getDataSheet(hitsMap, scoreInterval);
        dataSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet("scoreGraph").doWrite(dataSheet);
        log.info("export score graph success : " + outputFileName);
    }

    public void estimatedPValueGraph(String fileName, ConcurrentHashMap<String, List<LibraryHit>> hitsMap) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export estimatedPValue graph : " + outputFileName);
        List<List<Object>> dataSheet = getDataSheet(hitsMap, 1000);
        List<Double> pList = new ArrayList<>();
        for (int i = 20; i > 0; i--) {
            pList.add(i * 0.05);
        }

        for (int i = 0; i < dataSheet.size(); i++) {
            double pValue = (double) dataSheet.get(i).get(6);

        }

        EasyExcel.write(outputFileName).sheet("estimatedPValueGraph").doWrite(dataSheet);
        log.info("export estimatedPValue graph success : " + outputFileName);
    }

    private List<List<Object>> getDataSheet(ConcurrentHashMap<String, List<LibraryHit>> hitsMap, int scoreInterval) {
        List<List<Object>> dataSheet = new ArrayList<>();
        List<LibraryHit> allTargetHits = new ArrayList<>();
        //a hit in the following lists means the top score hit for a specific query spectrum
        List<LibraryHit> decoyHits = new ArrayList<>();
        List<LibraryHit> targetHits = new ArrayList<>();

        hitsMap.forEach((k, v) -> {
            if (v.size() != 0) {
                Map<Boolean, List<LibraryHit>> targetDecoyMap = v.stream().collect(Collectors.groupingBy(LibraryHit::isDecoy));
                for (Map.Entry<Boolean, List<LibraryHit>> entry : targetDecoyMap.entrySet()) {
                    if (entry.getKey()) {
                        targetDecoyMap.get(true).sort(Comparator.comparing(LibraryHit::getScore).reversed());
                        decoyHits.add(targetDecoyMap.get(true).get(0));
                    } else {
                        targetDecoyMap.get(false).sort(Comparator.comparing(LibraryHit::getScore).reversed());
                        allTargetHits.addAll(targetDecoyMap.get(false));
                        targetHits.add(targetDecoyMap.get(false).get(0));
                    }
                }
            }
        });

        decoyHits.sort(Comparator.comparing(LibraryHit::getScore));
        targetHits.sort(Comparator.comparing(LibraryHit::getScore));
        double minScore = Math.min(decoyHits.get(0).getScore(), targetHits.get(0).getScore());
        double maxScore = Math.max(decoyHits.get(decoyHits.size() - 1).getScore(), targetHits.get(targetHits.size() - 1).getScore());
        double step = (maxScore - minScore) / scoreInterval;

        for (int i = 0; i < scoreInterval; i++) {
            double finalMinScore = minScore + i * step;
            double finalMaxScore = minScore + (i + 1) * step;
            int targetCount, decoyCount, incorrectCount;
            List<Object> row = new ArrayList<>();

            targetCount = targetHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            decoyCount = decoyHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            incorrectCount = allTargetHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size() - targetCount;

            //calculate FDR and pValue and PIT
            double pit = (double) incorrectCount / (targetCount + incorrectCount);
            double fdr = (double) decoyCount / (targetCount + decoyCount) * pit;
            double pValue = (double) decoyCount / (targetCount + decoyCount);

            //calculate hits distribution
            if (i == 0) {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = decoyHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
            } else {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = decoyHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
            }

            //write data sheet
            row.add(finalMinScore);
            row.add(finalMaxScore);
            row.add((double) targetCount / targetHits.size());
            row.add((double) decoyCount / decoyHits.size());
            row.add((double) (targetCount + decoyCount) / (targetHits.size() + decoyHits.size()));
            row.add(fdr);
            row.add(pValue);
            row.add(pit);
            dataSheet.add(row);
        }
        return dataSheet;
    }
}
