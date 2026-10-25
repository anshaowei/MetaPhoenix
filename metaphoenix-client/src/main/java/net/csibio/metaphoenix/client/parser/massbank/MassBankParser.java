package net.csibio.metaphoenix.client.parser.massbank;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.enums.MsLevel;
import net.csibio.metaphoenix.client.constants.AdductConst;
import net.csibio.metaphoenix.client.constants.enums.IonMode;
import net.csibio.metaphoenix.client.domain.Result;
import net.csibio.metaphoenix.client.domain.bean.adduct.Adduct;
import net.csibio.metaphoenix.client.domain.db.LibraryDO;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.parser.common.MspParser;
import net.csibio.metaphoenix.client.service.LibraryService;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component("massBankParser")
@Slf4j
public class MassBankParser {

    @Autowired
    SpectrumService spectrumService;
    @Autowired
    LibraryService libraryService;

    public void parseMspEU(String filePath, String libraryName) {

        //read file use buffer
        File file = new File(filePath);
        FileInputStream fis;

        //create library
//        String libraryName = "MassBank-Europe";
        LibraryDO libraryDO = new LibraryDO();
        libraryDO.setName(libraryName);
        if (libraryService.insert(libraryDO).isFailed()) {
            log.error("Create library failed");
            Result.Error("Create library failed");
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
            log.info("Start importing, Total spectrum count: {}", spectrumCount);

            fis = new FileInputStream(file);
            br = new BufferedReader(new java.io.InputStreamReader(fis));
            line = br.readLine();
            List<SpectrumDO> spectrumDOS = new ArrayList<>();
            while (line != null) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.startsWith("name")) {
                    String value = MspParser.fieldValue(line);
                    SpectrumDO spectrumDO = new SpectrumDO();
                    if (value != null) {
                        spectrumDO.setCompoundName(value);
                    }
                    line = br.readLine();
                    while (line != null) {
                        lowerLine = line.toLowerCase();
                        //break if next spectrum
                        if (lowerLine.startsWith("name")) {
                            break;
                        }
                        //synon
                        if (lowerLine.startsWith("synon")) {
                            String synonValue = MspParser.fieldValue(line);
                            if (synonValue != null) {
                                spectrumDO.setSynon(synonValue);
                            }
                        }
                        //inchiKey
                        if (lowerLine.startsWith("inchikey")) {
                            String inchiKeyValue = MspParser.fieldValue(line);
                            if (inchiKeyValue != null) {
                                spectrumDO.setInChIKey(inchiKeyValue);
                            }
                        }
                        //inchi
                        if (lowerLine.startsWith("inchi")) {
                            String inchiValue = MspParser.fieldValue(line);
                            if (inchiValue != null) {
                                spectrumDO.setInChI(inchiValue);
                            }
                        }
                        //smiles
                        if (lowerLine.startsWith("smiles")) {
                            String smilesValue = MspParser.fieldValue(line);
                            if (smilesValue != null) {
                                spectrumDO.setSmiles(smilesValue);
                            }
                        }
                        //precursor_type
                        if (lowerLine.startsWith("precursor_type")) {
                            String precursorTypeValue = MspParser.fieldValue(line);
                            if (precursorTypeValue != null) {
                                spectrumDO.setPrecursorAdduct(precursorTypeValue);
                            }
                        }
                        //spectrum_type
                        if (lowerLine.startsWith("spectrum_type")) {
                            String spectrumTypeValue = MspParser.fieldValue(line);
                            if (spectrumTypeValue != null) {
                                if (spectrumTypeValue.equals("MS1")) {
                                    spectrumDO.setMsLevel(MsLevel.MS1.getCode());
                                }
                                if (spectrumTypeValue.equals("MS2")) {
                                    spectrumDO.setMsLevel(MsLevel.MS2.getCode());
                                }
                                if (spectrumTypeValue.equals("MS3")) {
                                    break;
                                }
                            }
                        }
                        //precursorMz
                        if (lowerLine.startsWith("precursormz")) {
                            String precursorMzValue = MspParser.fieldValue(line);
                            if (precursorMzValue != null) {
                                Double precursorMz = null;
                                try {
                                    precursorMz = Double.parseDouble(precursorMzValue);
                                } catch (Exception e) {
//                                    log.error("Parse precursorMz failed, precursorMz: {} is not number", precursorMzValue);
                                }
                                spectrumDO.setPrecursorMz(precursorMz);
                            }
                        }
                        //instrument_type
                        if (lowerLine.startsWith("instrument_type")) {
                            String instrumentTypeValue = MspParser.fieldValue(line);
                            if (instrumentTypeValue != null) {
                                spectrumDO.setInstrumentType(instrumentTypeValue);
                            }
                        }
                        //instrument
                        if (lowerLine.startsWith("instrument")) {
                            String instrumentValue = MspParser.fieldValue(line);
                            if (instrumentValue != null) {
                                spectrumDO.setInstrument(instrumentValue);
                            }
                        }
                        //ion_mode
                        if (lowerLine.startsWith("ion_mode")) {
                            String ionModeValue = MspParser.fieldValue(line);
                            if (ionModeValue != null) {
                                if (ionModeValue.equalsIgnoreCase("positive")) {
                                    spectrumDO.setIonMode(IonMode.Positive.getName());
                                }
                                if (ionModeValue.equalsIgnoreCase("negative")) {
                                    spectrumDO.setIonMode(IonMode.Negative.getName());
                                }
                            }
                        }
                        //collision_energy
                        if (lowerLine.startsWith("collision_energy")) {
                            String collisionEnergyValue = MspParser.fieldValue(line);
                            if (collisionEnergyValue != null) {
                                Double collisionEnergy = null;
                                try {
                                    collisionEnergy = Double.valueOf(collisionEnergyValue);
                                } catch (Exception e) {
//                                    log.error("Parse collision energy failed, value: {}", collisionEnergyValue);
                                }
                                spectrumDO.setCollisionEnergy(collisionEnergy);
                            }
                        }
                        //formula
                        if (lowerLine.startsWith("formula")) {
                            String formulaValue = MspParser.fieldValue(line);
                            if (formulaValue != null) {
                                spectrumDO.setFormula(formulaValue);
                            }
                        }
                        //mw
                        if (lowerLine.startsWith("mw")) {
                            String mwValue = MspParser.fieldValue(line);
                            if (mwValue != null) {
                            }
                        }
                        //exactMass
                        if (lowerLine.startsWith("exactmass")) {
                            String exactMassValue = MspParser.fieldValue(line);
                            if (exactMassValue != null) {
                                spectrumDO.setExactMass(Double.valueOf(exactMassValue));
                            }
                        }
                        //comments
                        if (lowerLine.startsWith("comments")) {
                            String commentsValue = MspParser.fieldValue(line);
                            if (commentsValue != null) {
                                spectrumDO.setComment(commentsValue);
                            }
                        }
                        //splash
                        if (lowerLine.startsWith("splash")) {
                            String splashValue = MspParser.fieldValue(line);
                            if (splashValue != null) {
                                spectrumDO.setSplash(splashValue);
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
                    if (spectrumDO.getMsLevel() != null) {
//                        if (spectrumDO.getPrecursorMz() == null) {
//                            Double precursorMz = calculatePrecursorMz(spectrumDO.getPrecursorAdduct(), spectrumDO.getExactMass(), spectrumDO.getIonMode());
//                            if (precursorMz != null) {
//                                spectrumDO.setPrecursorMz(precursorMz);
//                                log.info("calculate precursor success: {}", precursorMz);
//                            }
//                        }
                        spectrumDO.setLibraryId(libraryDO.getId());
                        spectrumDOS.add(spectrumDO);
                    }
                } else {
                    line = br.readLine();
                }
            }
            spectrumService.insert(spectrumDOS, libraryName);
            log.info("Finish importing, inserted spectrum count: {}", spectrumDOS.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void parseMspMoNA(String filePath) {
        parseMspMoNA(filePath, "MassBank-MoNA");
    }

    public void parseMspMoNA(String filePath, String libraryName) {
        //read file use buffer
        File file = new File(filePath);
        FileInputStream fis = null;
        int count = 0;

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
            log.info("Start importing, Total spectrum count: {}", spectrumCount);
            spectrumCount = 0;

            fis = new FileInputStream(file);
            br = new BufferedReader(new java.io.InputStreamReader(fis));
            line = br.readLine();
            List<SpectrumDO> spectrumDOS = new ArrayList<>();
            while (line != null) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.startsWith("name")) {
                    String value = MspParser.fieldValue(line);
                    SpectrumDO spectrumDO = new SpectrumDO();
                    if (value != null) {
                        spectrumDO.setCompoundName(value);
                    }
                    line = br.readLine();
                    while (line != null) {
                        lowerLine = line.toLowerCase();
                        //break if next spectrum
                        if (lowerLine.startsWith("name")) {
                            break;
                        }
                        //synon
                        if (lowerLine.startsWith("synon")) {
                            String synonValue = MspParser.fieldValue(line);
                            if (synonValue != null) {
                                spectrumDO.setSynon(synonValue);
                            }
                        }
                        //inchiKey
                        if (lowerLine.startsWith("inchikey")) {
                            String inchiKeyValue = MspParser.fieldValue(line);
                            if (inchiKeyValue != null) {
                                spectrumDO.setInChIKey(inchiKeyValue);
                            }
                        }
                        //precursor_type
                        if (lowerLine.startsWith("precursor_type")) {
                            String precursorTypeValue = MspParser.fieldValue(line);
                            if (precursorTypeValue != null) {
                                spectrumDO.setPrecursorAdduct(precursorTypeValue);
                            }
                        }
                        //precursor_mz
                        if (lowerLine.startsWith("precursormz")) {
                            String precursorMzValue = MspParser.fieldValue(line);
                            if (precursorMzValue != null) {
                                try {
                                    spectrumDO.setPrecursorMz(Double.valueOf(precursorMzValue));
                                } catch (NumberFormatException e) {
//                                    log.error("precursorMz is not a number: {}", precursorMzValue);
                                }
                            }
                        }
                        //spectrum_type
                        if (lowerLine.startsWith("spectrum_type")) {
                            String spectrumTypeValue = MspParser.fieldValue(line);
                            if (spectrumTypeValue != null) {
                                if (spectrumTypeValue.equals("MS1")) {
                                    spectrumDO.setMsLevel(MsLevel.MS1.getCode());
                                }
                                if (spectrumTypeValue.equals("MS2")) {
                                    spectrumDO.setMsLevel(MsLevel.MS2.getCode());
                                }
                                if (spectrumTypeValue.equals("MS3")) {
                                    break;
                                }
                            }
                        }
                        //instrument_type
                        if (lowerLine.startsWith("instrument_type")) {
                            String instrumentTypeValue = MspParser.fieldValue(line);
                            if (instrumentTypeValue != null) {
                                spectrumDO.setInstrumentType(instrumentTypeValue);
                            }
                        }
                        //instrument
                        if (lowerLine.startsWith("instrument")) {
                            String instrumentValue = MspParser.fieldValue(line);
                            if (instrumentValue != null) {
                                spectrumDO.setInstrument(instrumentValue);
                            }
                        }
                        //ion_mode
                        if (lowerLine.startsWith("ion_mode")) {
                            String ionModeValue = MspParser.fieldValue(line);
                            if (ionModeValue != null) {
                                if (ionModeValue.equalsIgnoreCase("positive") || ionModeValue.equalsIgnoreCase("p")) {
                                    spectrumDO.setIonMode(IonMode.Positive.getName());
                                }
                                if (ionModeValue.equalsIgnoreCase("negative") || ionModeValue.equalsIgnoreCase("n")) {
                                    spectrumDO.setIonMode(IonMode.Negative.getName());
                                }
                            }
                        }
                        //formula
                        if (lowerLine.startsWith("formula")) {
                            String formulaValue = MspParser.fieldValue(line);
                            if (formulaValue != null) {
                                spectrumDO.setFormula(formulaValue);
                            }
                        }
                        //mw
                        if (lowerLine.startsWith("mw")) {
                            String mwValue = MspParser.fieldValue(line);
                            if (mwValue != null) {
                            }
                        }
                        //exactMass
                        if (lowerLine.startsWith("exactmass")) {
                            String exactMassValue = MspParser.fieldValue(line);
                            if (exactMassValue != null) {
                                spectrumDO.setExactMass(Double.valueOf(exactMassValue));
                            }
                        }
                        //comments
                        if (lowerLine.startsWith("comments")) {
                            String[] commentsItems = line.split(" ");
                            if (commentsItems.length > 1) {
                                for (int i = 1; i < commentsItems.length; i++) {
                                    if (commentsItems[i].length() > 3) {
                                        commentsItems[i] = commentsItems[i].substring(1, commentsItems[i].length() - 1);
                                        if (commentsItems[i].toLowerCase().startsWith("smiles")) {
                                            spectrumDO.setSmiles(commentsItems[i].substring(7));
                                            break;
                                        }
                                    }
                                }
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
                    if (spectrumDO.getMsLevel() != null) {
//                        if (spectrumDO.getPrecursorMz() == null) {
//                            Double precursorMz = calculatePrecursorMz(spectrumDO.getPrecursorAdduct(), spectrumDO.getExactMass(), spectrumDO.getIonMode());
//                            if (precursorMz != null) {
//                                spectrumDO.setPrecursorMz(precursorMz);
//                            }
//                        }
                        spectrumDO.setLibraryId(libraryDO.getId());
                        spectrumDOS.add(spectrumDO);
                        spectrumCount++;
                    }
                } else {
                    line = br.readLine();
                }
            }
            spectrumService.insert(spectrumDOS, libraryName);
            log.info("Finish importing, inserted spectrum count: {}", spectrumCount);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Double calculatePrecursorMz(String precursorAdduct, Double exactMass, String ionMode) {
        if (precursorAdduct == null || exactMass == null || ionMode == null) {
            return null;
        }
        if (!ionMode.equals(IonMode.Positive.getName()) || !ionMode.equals(IonMode.Negative.getName())) {
            return null;
        }
        Double precursorMz = null;
        List<Adduct> adducts = (ionMode.equals(IonMode.Positive.getName())) ? AdductConst.ESIAdducts_Positive : AdductConst.ESIAdducts_Negative;
        for (Adduct adduct : adducts) {
            if (adduct.getIonForm().contains(precursorAdduct)) {
                precursorMz = adduct.getPrecursorMz(exactMass);
                return precursorMz;
            }
        }
        return precursorMz;
    }

}
