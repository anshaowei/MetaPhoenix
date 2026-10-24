package net.csibio.mslibrary.client.parser.common;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.enums.MsLevel;
import net.csibio.mslibrary.client.constants.enums.IonMode;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component("mspParser")
@Slf4j
public class MspParser {

    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;

    public void execute(String filePath, String libraryName) {
        //read file use buffer
        File file = new File(filePath);
        FileInputStream fis;
        log.info("Start importing library: " + libraryName);

        //create library
        LibraryDO libraryDO = new LibraryDO();
        libraryDO.setName(libraryName);
        if (libraryService.insert(libraryDO).isFailed()) {
            log.error("Create library failed");
            return;
        }
        log.info("Create library success, library id: {}", libraryDO.getId());

        try {
            //fast read of spectra information
            fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new java.io.InputStreamReader(fis));
            String line = null;
            Integer spectrumCount = 0;
            while ((line = br.readLine()) != null) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.startsWith("name")) {
                    spectrumCount++;
                }
            }
            br.close();
            fis.close();
            log.info("Pre scan: total spectrum count: {}", spectrumCount);

            fis = new FileInputStream(file);
            br = new BufferedReader(new java.io.InputStreamReader(fis));
            line = br.readLine();
            List<SpectrumDO> spectrumDOS = new ArrayList<>();
            while (line != null) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.startsWith("name")) {
                    SpectrumDO spectrumDO = new SpectrumDO();
                    spectrumDO.setLibraryId(libraryDO.getId());
                    spectrumDO.setCompoundName(fieldValue(line));
                    line = br.readLine();
                    while (line != null) {
                        lowerLine = line.toLowerCase();
                        //break if next spectrum
                        if (lowerLine.startsWith("name")) {
                            break;
                        }
                        //precursor m/z
                        if (lowerLine.startsWith("precursormz")) {
                            String value = fieldValue(line);
                            if (value != null) {
                                spectrumDO.setPrecursorMz(Double.parseDouble(value));
                            }
                        }
                        //precursor type
                        else if (lowerLine.startsWith("precursortype")) {
                            String value = fieldValue(line);
                            if (value != null) {
                                spectrumDO.setPrecursorAdduct(value);
                            }
                        }
                        //formula
                        else if (lowerLine.startsWith("formula")) {
                            String value = fieldValue(line);
                            if (value != null) {
                                spectrumDO.setFormula(value);
                            }
                        }
                        //ontology
                        else if (lowerLine.startsWith("ontology")) {
                            String value = fieldValue(line);
                            if (value != null) {
                                spectrumDO.setOntology(value);
                            }
                        }
                        //inchiKey
                        else if (lowerLine.startsWith("inchikey")) {
                            String value = fieldValue(line);
                            if (value != null) {
                                spectrumDO.setInChIKey(value);
                            }
                        }
                        //inchi
                        else if (lowerLine.startsWith("inchi")) {
                            String value = fieldValue(line);
                            if (value != null) {
                                spectrumDO.setInChI(value);
                            }
                        }
                        //smiles
                        else if (lowerLine.startsWith("smiles")) {
                            String value = fieldValue(line);
                            if (value != null) {
                                spectrumDO.setSmiles(value);
                            }
                        }
                        //retention time
                        else if (lowerLine.startsWith("retentiontime")) {
                            String value = fieldValue(line);
                            if (value != null) {
                            }
                        }
                        //ionMode
                        else if (lowerLine.startsWith("ionmode")) {
                            String value = fieldValue(line);
                            if (value != null) {
                                if (value.equalsIgnoreCase(IonMode.Positive.getName())) {
                                    spectrumDO.setIonMode(IonMode.Positive.getName());
                                }
                                if (value.equalsIgnoreCase(IonMode.Negative.getName())) {
                                    spectrumDO.setIonMode(IonMode.Negative.getName());
                                }
                            }
                        }
                        //instrumentType
                        else if (lowerLine.startsWith("instrumenttype")) {
                            String value = fieldValue(line);
                            if (value != null) {
                                spectrumDO.setInstrumentType(value);
                            }
                        }
                        //instrument
                        else if (lowerLine.startsWith("instrument")) {
                            String value = fieldValue(line);
                            if (value != null) {
                                spectrumDO.setInstrument(value);
                            }
                        }
                        //collisionEnergy
                        else if (lowerLine.startsWith("collisionenergy")) {
                            String value = fieldValue(line);
                            Double collisionEnergy = null;
                            if (value != null) {
                                try {
                                    collisionEnergy = Double.parseDouble(value);
                                } catch (Exception e) {
                                    log.error("Collision energy parse error: {}", spectrumDO.getCompoundName());
                                }
                                spectrumDO.setCollisionEnergy(collisionEnergy);
                            }
                        }
                        //comment
                        else if (lowerLine.startsWith("comment")) {
                            String value = fieldValue(line);
                            if (value != null) {
                                spectrumDO.setComment(value);
                            }
                        }
                        //num peaks
                        else if (lowerLine.startsWith("num peaks")) {
                            List<Double> mzList = new ArrayList<>();
                            List<Double> intensityList = new ArrayList<>();
                            line = br.readLine();
                            while (line != null && !line.isEmpty()) {
                                String[] values = line.trim().split("\\s+");
                                if (values.length > 1) {
                                    double mz = Double.parseDouble(values[0]);
                                    double intensity = Double.parseDouble(values[1]);
                                    mzList.add(mz);
                                    intensityList.add(intensity);
                                }
                                line = br.readLine();
                            }
                            double[] mzArray = new double[mzList.size()];
                            double[] intensityArray = new double[intensityList.size()];
                            for (int i = 0; i < mzList.size(); i++) {
                                mzArray[i] = mzList.get(i);
                                intensityArray[i] = intensityList.get(i);
                            }
                            spectrumDO.setMzs(mzArray);
                            spectrumDO.setInts(intensityArray);
                        }
                        line = br.readLine();
                    }
                    spectrumDO.setMsLevel(MsLevel.MS2.getCode());
                    spectrumDOS.add(spectrumDO);
                } else {
                    line = br.readLine();
                }
            }
            fis.close();
            br.close();
            spectrumService.insert(spectrumDOS, libraryDO.getId());
            log.info("Finish library importing, inserted spectrum count: {}", spectrumDOS.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Value of a "Key: value" line. Everything after the first colon (or the first space when there is no
     * colon) is returned, so values containing spaces stay intact. Returns null when the value is empty.
     */
    static String fieldValue(String line) {
        int sep = line.indexOf(':');
        if (sep < 0) {
            sep = line.indexOf(' ');
        }
        if (sep < 0) {
            return null;
        }
        String value = line.substring(sep + 1).trim();
        return value.isEmpty() ? null : value;
    }
}
