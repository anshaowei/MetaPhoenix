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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void scoreGraph(String fileName, ConcurrentHashMap<String, List<LibraryHit>> hitsMap) {
        //init
        double interval = 100;
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        List<List<Object>> datasheet = new ArrayList<>();
        //header
        List<Object> header = Arrays.asList("score", "target", "decoy", "total", "FDR");
        List<LibraryHit> decoyHits = new ArrayList<>();
        List<LibraryHit> targetHits = new ArrayList<>();
        AtomicInteger correct = new AtomicInteger();
        AtomicInteger incorrect = new AtomicInteger();

        hitsMap.forEach((k, v) -> {
            if (v.size() != 0) {
                correct.getAndIncrement();
                for (LibraryHit hit : v) {
                    if (hit.isDecoy()) {
                        decoyHits.add(hit);
                    } else {
                        targetHits.add(hit);
                        incorrect.getAndIncrement();
                    }
                }
            }
        });
        incorrect.set(incorrect.get() - correct.get());
        double pit = (double) incorrect.get() / (incorrect.get() + correct.get());

        decoyHits.sort(Comparator.comparing(LibraryHit::getScore));
        targetHits.sort(Comparator.comparing(LibraryHit::getScore));
        double minScore = Math.min(decoyHits.get(0).getScore(), targetHits.get(0).getScore());
        double maxScore = Math.max(decoyHits.get(decoyHits.size() - 1).getScore(), targetHits.get(targetHits.size() - 1).getScore());
        double step = (maxScore - minScore) / interval;

        for (int i = 0; i < interval; i++) {
            double finalMinScore = minScore + i * step;
            double finalMaxScore = minScore + (i + 1) * step;
            int targetCount, decoyCount;
            List<Object> row = new ArrayList<>();

            targetCount = targetHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            decoyCount = decoyHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();

            //calculate FDR
            double fdr = 0.0;
            fdr = (double) decoyCount / targetCount * pit;

            //calculate hits distribution
            if (i == 0) {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = decoyHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
            } else {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = decoyHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
            }

            row.add(finalMaxScore);
            row.add((double) targetCount / targetHits.size());
            row.add((double) decoyCount / decoyHits.size());
            row.add((double) (targetCount + decoyCount) / (targetHits.size() + decoyHits.size()));
            row.add(fdr);
            datasheet.add(row);
        }
        EasyExcel.write(outputFileName).sheet("scoreGraph").doWrite(datasheet);
        log.info("export score graph success : " + outputFileName);
    }

    public void estimatedPValueGraph(String fileName, ConcurrentHashMap<String, List<LibraryHit>> hitsMap) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        List<List<Object>> dataSheet = new ArrayList<>();


        EasyExcel.write(outputFileName).sheet("estimatedPValueGraph").doWrite(dataSheet);
    }
}
