package net.csibio.mslibrary.client.export;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component("reporter")
@Slf4j
public class Reporter {

    public Result toExcel(String outputName, List<LibraryHit> libraryHits) {
        EasyExcel.write(outputName, LibraryHit.class).sheet("result").doWrite(libraryHits);
        return null;
    }

    public Result toMsp(String outputName, List<SpectrumDO> spectrumDOS) throws IOException {
        FileWriter fileWriter = new FileWriter(outputName);
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
        }
        bufferedWriter.close();
        fileWriter.close();
        log.info("export msp file success : " + outputName);
        return new Result(true);
    }
}