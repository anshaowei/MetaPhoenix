package net.csibio.mslibrary.core.export;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.core.config.VMProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component("reporter")
@Slf4j
public class Reporter {

    @Autowired
    VMProperties vmProperties;

    public Result toExcel(String fileName, List<LibraryHit> libraryHits) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        EasyExcel.write(outputFileName, LibraryHit.class).sheet("result").doWrite(libraryHits);
        return null;
    }

    public Result toMsp(String fileName, List<SpectrumDO> spectrumDOS) {
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

    public Result toMgf(String fileName, List<SpectrumDO> spectrumDOS) {
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
}
