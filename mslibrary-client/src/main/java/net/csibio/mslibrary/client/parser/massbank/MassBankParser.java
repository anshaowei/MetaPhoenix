package net.csibio.mslibrary.client.parser.massbank;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.enums.MsLevel;
import net.csibio.mslibrary.client.constants.AdductConst;
import net.csibio.mslibrary.client.constants.enums.IonMode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.adduct.Adduct;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
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

    public void parseMspEU(String filePath) {

        //read file use buffer
        File file = new File(filePath);
        FileInputStream fis = null;

        //create library
        String libraryName = "MassBank-Europe";
        LibraryDO libraryDO = new LibraryDO();
        libraryDO.setName(libraryName);
        if (libraryService.insert(libraryDO).isFailed()) {
            log.error("Create library failed");
            Result.Error("Create library failed");
            return;
        }

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
                    String[] items = line.split(" ");
                    SpectrumDO spectrumDO = new SpectrumDO();
                    if (items.length > 1) {
                        spectrumDO.setCompoundName(items[1]);
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
                            String[] synonItems = line.split(" ");
                            if (synonItems.length > 1) {
                                spectrumDO.setSynon(synonItems[1]);
                            }
                        }
                        //inchiKey
                        if (lowerLine.startsWith("inchikey")) {
                            String[] inchiKeyItems = line.split(" ");
                            if (inchiKeyItems.length > 1) {
                                spectrumDO.setInchiKeyInchi(inchiKeyItems[1]);
                            }
                        }
                        //inchi
                        if (lowerLine.startsWith("inchi")) {
                            String[] inchiItems = line.split(" ");
                            if (inchiItems.length > 1) {
                                spectrumDO.setInChI(inchiItems[1]);
                            }
                        }
                        //smiles
                        if (lowerLine.startsWith("smiles")) {
                            String[] smilesItems = line.split(" ");
                            if (smilesItems.length > 1) {
                                spectrumDO.setSmiles(smilesItems[1]);
                            }
                        }
                        //precursor_type
                        if (lowerLine.startsWith("precursor_type")) {
                            String[] precursorTypeItems = line.split(" ");
                            if (precursorTypeItems.length > 1) {
                                spectrumDO.setPrecursorAdduct(precursorTypeItems[1]);
                            }
                        }
                        //spectrum_type
                        if (lowerLine.startsWith("spectrum_type")) {
                            String[] spectrumTypeItems = line.split(" ");
                            if (spectrumTypeItems.length > 1) {
                                if (spectrumTypeItems[1].equals("MS1")) {
                                    spectrumDO.setMsLevel(MsLevel.MS1.getCode());
                                }
                                if (spectrumTypeItems[1].equals("MS2")) {
                                    spectrumDO.setMsLevel(MsLevel.MS2.getCode());
                                }
                                if (spectrumTypeItems[1].equals("MS3")) {
                                    break;
                                }
                            }
                        }
                        //precursorMz
                        if (lowerLine.startsWith("precursormz")) {
                            String[] precursorMzItems = line.split(" ");
                            if (precursorMzItems.length > 1) {
                                Double precursorMz = null;
                                try {
                                    precursorMz = Double.parseDouble(precursorMzItems[1]);
                                } catch (Exception e) {
                                    log.error("Parse precursorMz failed, precursorMz: {} is not number", precursorMzItems[1]);
                                }
                                spectrumDO.setPrecursorMz(precursorMz);
                            }
                        }
                        //instrument_type
                        if (lowerLine.startsWith("instrument_type")) {
                            String[] instrumentTypeItems = line.split(" ");
                            if (instrumentTypeItems.length > 1) {
                                spectrumDO.setInstrumentType(instrumentTypeItems[1]);
                            }
                        }
                        //instrument
                        if (lowerLine.startsWith("instrument")) {
                            String[] instrumentItems = line.split(" ");
                            if (instrumentItems.length > 1) {
                                spectrumDO.setInstrument(instrumentItems[1]);
                            }
                        }
                        //ion_mode
                        if (lowerLine.startsWith("ion_mode")) {
                            String[] ionModeItems = line.split(" ");
                            if (ionModeItems.length > 1) {
                                if (ionModeItems[1].equalsIgnoreCase("positive")) {
                                    spectrumDO.setIonMode(IonMode.Positive.getName());
                                }
                                if (ionModeItems[1].equalsIgnoreCase("negative")) {
                                    spectrumDO.setIonMode(IonMode.Negative.getName());
                                }
                            }
                        }
                        //collision_energy
                        if (lowerLine.startsWith("collision_energy")) {
                            String[] collisionEnergyItems = line.split(" ");
                            if (collisionEnergyItems.length > 1) {
                                Double collisionEnergy = null;
                                try {
                                    collisionEnergy = Double.valueOf(collisionEnergyItems[1]);
                                } catch (Exception e) {
//                                    log.error("Parse collision energy failed, value: {}", collisionEnergyItems[1]);
                                }
                                spectrumDO.setCollisionEnergy(collisionEnergy);
                            }
                        }
                        //formula
                        if (lowerLine.startsWith("formula")) {
                            String[] formulaItems = line.split(" ");
                            if (formulaItems.length > 1) {
                                spectrumDO.setFormula(formulaItems[1]);
                            }
                        }
                        //mw
                        if (lowerLine.startsWith("mw")) {
                            String[] mwItems = line.split(" ");
                            if (mwItems.length > 1) {
                            }
                        }
                        //exactMass
                        if (lowerLine.startsWith("exactmass")) {
                            String[] exactMassItems = line.split(" ");
                            if (exactMassItems.length > 1) {
                                spectrumDO.setExactMass(Double.valueOf(exactMassItems[1]));
                            }
                        }
                        //comments
                        if (lowerLine.startsWith("comments")) {
                            String[] commentsItems = line.split(" ");
                            if (commentsItems.length > 1) {
                                spectrumDO.setComment(commentsItems[1]);
                            }
                        }
                        //splash
                        if (lowerLine.startsWith("splash")) {
                            String[] splashItems = line.split(" ");
                            if (splashItems.length > 1) {
                                spectrumDO.setSplash(splashItems[1]);
                            }
                        }
                        //num peaks
                        else if (lowerLine.startsWith("num peaks")) {
                            List<Double> mzList = new ArrayList<>();
                            List<Double> intensityList = new ArrayList<>();
                            line = br.readLine();
                            while (line != null && !line.isEmpty()) {
                                String[] values = line.split(" ");
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
                    if (spectrumDO.getMsLevel() != null && spectrumDO.getPrecursorMz() != null) {
                        spectrumDO.setLibraryId(libraryDO.getId());
                        spectrumDOS.add(spectrumDO);
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

    public void parseMspMoNA(String filePath) {
        //read file use buffer
        File file = new File(filePath);
        FileInputStream fis = null;
        int count = 0;

        //create library
        String libraryName = "MassBank-MoNA";
        LibraryDO libraryDO = new LibraryDO();
        libraryDO.setName(libraryName);
        if (libraryService.insert(libraryDO).isFailed()) {
            log.error("Create library failed");
            return;
        }

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
                    String[] items = line.split(" ");
                    SpectrumDO spectrumDO = new SpectrumDO();
                    if (items.length > 1) {
                        spectrumDO.setCompoundName(items[1]);
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
                            String[] synonItems = line.split(" ");
                            if (synonItems.length > 1) {
                                spectrumDO.setSynon(synonItems[1]);
                            }
                        }
                        //inchiKey
                        if (lowerLine.startsWith("inchikey")) {
                            String[] inchiKeyItems = line.split(" ");
                            if (inchiKeyItems.length > 1) {
                                spectrumDO.setInchiKeyInchi(inchiKeyItems[1]);
                            }
                        }
                        //precursor_type
                        if (lowerLine.startsWith("precursor_type")) {
                            String[] precursorTypeItems = line.split(" ");
                            if (precursorTypeItems.length > 1) {
                                spectrumDO.setPrecursorAdduct(precursorTypeItems[1]);
                            }
                        }
                        //precursor_mz
                        if (lowerLine.startsWith("precursormz")) {
                            String[] precursorMzItems = line.split(" ");
                            if (precursorMzItems.length > 1) {
                                try {
                                    spectrumDO.setPrecursorMz(Double.valueOf(precursorMzItems[1]));
                                } catch (NumberFormatException e) {
//                                    log.error("precursorMz is not a number: {}", precursorMzItems[1]);
                                }
                            }
                        }
                        //spectrum_type
                        if (lowerLine.startsWith("spectrum_type")) {
                            String[] spectrumTypeItems = line.split(" ");
                            if (spectrumTypeItems.length > 1) {
                                if (spectrumTypeItems[1].equals("MS1")) {
                                    spectrumDO.setMsLevel(MsLevel.MS1.getCode());
                                }
                                if (spectrumTypeItems[1].equals("MS2")) {
                                    spectrumDO.setMsLevel(MsLevel.MS2.getCode());
                                }
                                if (spectrumTypeItems[1].equals("MS3")) {
                                    break;
                                }
                            }
                        }
                        //instrument_type
                        if (lowerLine.startsWith("instrument_type")) {
                            String[] instrumentTypeItems = line.split(" ");
                            if (instrumentTypeItems.length > 1) {
                                spectrumDO.setInstrumentType(instrumentTypeItems[1]);
                            }
                        }
                        //instrument
                        if (lowerLine.startsWith("instrument")) {
                            String[] instrumentItems = line.split(" ");
                            if (instrumentItems.length > 1) {
                                spectrumDO.setInstrument(instrumentItems[1]);
                            }
                        }
                        //ion_mode
                        if (lowerLine.startsWith("ion_mode")) {
                            String[] ionModeItems = line.split(" ");
                            if (ionModeItems.length > 1) {
                                if (ionModeItems[1].equalsIgnoreCase("positive") || ionModeItems[1].equalsIgnoreCase("p")) {
                                    spectrumDO.setIonMode(IonMode.Positive.getName());
                                }
                                if (ionModeItems[1].equalsIgnoreCase("negative") || ionModeItems[1].equalsIgnoreCase("n")) {
                                    spectrumDO.setIonMode(IonMode.Negative.getName());
                                }
                            }
                        }
                        //formula
                        if (lowerLine.startsWith("formula")) {
                            String[] formulaItems = line.split(" ");
                            if (formulaItems.length > 1) {
                                spectrumDO.setFormula(formulaItems[1]);
                            }
                        }
                        //mw
                        if (lowerLine.startsWith("mw")) {
                            String[] mwItems = line.split(" ");
                            if (mwItems.length > 1) {
                            }
                        }
                        //exactMass
                        if (lowerLine.startsWith("exactmass")) {
                            String[] exactMassItems = line.split(" ");
                            if (exactMassItems.length > 1) {
                                spectrumDO.setExactMass(Double.valueOf(exactMassItems[1]));
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
                                String[] values = line.split(" ");
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
                        if (spectrumDO.getPrecursorMz() == null) {
                            if (spectrumDO.getExactMass() != null && spectrumDO.getPrecursorAdduct() != null && spectrumDO.getIonMode() != null) {
                                String precursorAdduct = spectrumDO.getPrecursorAdduct();
                                List<Adduct> adducts = (spectrumDO.getIonMode().equals(IonMode.Positive.getName())) ? AdductConst.ESIAdducts_Positive : AdductConst.ESIAdducts_Negative;
                                for (Adduct adduct : adducts) {
                                    if (adduct.getIonForm().contains(precursorAdduct)) {
                                        spectrumDO.setPrecursorMz(adduct.getPrecursorMz(spectrumDO.getExactMass()));
                                        break;
                                    }
                                }
                            }
                        }
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

}
