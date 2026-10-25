package net.csibio.metaphoenix.client.parser.gnps;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.enums.MsLevel;
import net.csibio.metaphoenix.client.constants.enums.IonMode;
import net.csibio.metaphoenix.client.constants.enums.LibraryType;
import net.csibio.metaphoenix.client.domain.bean.spectrum.AnnotationHistory;
import net.csibio.metaphoenix.client.domain.db.LibraryDO;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.service.CompoundService;
import net.csibio.metaphoenix.client.service.LibraryService;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Component("gnpsParser")
@Slf4j
public class GnpsParser {

    @Autowired
    CompoundService compoundService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;

    public void parseJSON(String filePath) {

        long startTime = System.currentTimeMillis();
        JsonFactory jsonFactory = new MappingJsonFactory();
        JsonParser parser;

        try {
            parser = jsonFactory.createParser(new File(filePath));
            int spectrumCount = 0;
            log.info("Start parsing JSON file: " + filePath);

            //pre scan
            HashSet<String> libraryNames = new HashSet<>();

            while (!parser.isClosed()) {
                JsonToken jsonToken = parser.nextToken();
                if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                    if (parser.getCurrentName().equals("spectrum_id")) {
                        spectrumCount++;
                    }
                    if (parser.getCurrentName().equals("library_membership")) {
                        parser.nextToken();
                        libraryNames.add(parser.getValueAsString());
                    }
                }
            }
            log.info("Pre scan: File contains about " + libraryNames.size() + " libraries, " + spectrumCount + " spectra");

            //generate library
            List<LibraryDO> libraryDOS = new ArrayList<>();
            for (String libraryName : libraryNames) {
                LibraryDO libraryDO = new LibraryDO();
                libraryDO.setName(libraryName);
                libraryDO.setType(LibraryType.Metabolomics.getName());
                libraryDOS.add(libraryDO);
            }

            //insert library
            if (libraryService.insert(libraryDOS).isFailed()) {
                log.error("Insert library failed, library with the same name already exist");
                return;
            }
            log.info("Insert library success, " + libraryDOS.size() + " libraries inserted");

            HashMap<String, List<SpectrumDO>> spectrumMap = new HashMap<>();
            for (String libraryName : libraryNames) {
                spectrumMap.put(libraryName, new ArrayList<>());
            }

            parser = jsonFactory.createParser(new File(filePath));
            while (!parser.isClosed()) {
                JsonToken jsonToken = parser.nextToken();
                if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                    if (parser.getCurrentName().equals("spectrum_id")) {
                        SpectrumDO spectrumDO = new SpectrumDO();
                        parser.nextToken();
                        spectrumDO.setSpectrumId(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setSourceFile(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setTask(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setScan(parser.getValueAsInt());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setMsLevel(parser.getValueAsInt());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setLibraryMembership(parser.getValueAsString());
                        spectrumDO.setLibraryId(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setSpectrumStatus(parser.getValueAsInt());
                        parser.nextToken();
                        parser.nextToken();
                        String[] values = parser.getValueAsString().replace("[", "").replace("]", "").replace(" ", "").replace(",", " ").split(" ");
                        int length = values.length / 2;
                        double[] mzs = new double[length];
                        double[] intensities = new double[length];
                        for (int k = 0; k < length; k++) {
                            mzs[k] = Double.parseDouble(values[2 * k]);
                            intensities[k] = Double.parseDouble(values[2 * k + 1]);
                        }
                        spectrumDO.setMzs(mzs);
                        spectrumDO.setInts(intensities);
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setSplash(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setSubmitUser(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setCompoundName(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setIonSource(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setCompoundSource(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setInstrument(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setPi(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setDataCollector(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setPrecursorAdduct(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setPrecursorMz(parser.getValueAsDouble());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setExactMass(parser.getValueAsDouble());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setCharge(parser.getValueAsInt());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setCasNumber(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setPubmedId(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setSmiles(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setInChI(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setInchiAUX(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setLibraryClass(parser.getValueAsInt());
                        parser.nextToken();
                        parser.nextToken();
                        parser.nextToken();
                        parser.nextToken();
                        if (parser.getValueAsString().equalsIgnoreCase(IonMode.Positive.getName())) {
                            spectrumDO.setIonSource(IonMode.Positive.getName());
                        } else if (parser.getValueAsString().equalsIgnoreCase(IonMode.Negative.getName())) {
                            spectrumDO.setIonSource(IonMode.Negative.getName());
                        }
                        parser.nextToken();
                        parser.nextToken();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        spectrumDO.setCreateDate(simpleDateFormat.parse(parser.getValueAsString()));
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setTaskId(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setUserId(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setInchiKeySmiles(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setInchiKeyInchi(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setFormulaSmiles(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setFormulaInchi(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setUrl(parser.getValueAsString());
                        List<AnnotationHistory> annotationHistoryList = new ArrayList<>();
                        //jsonToken = parser.currentToken();
                        while (!parser.isClosed()) {
                            jsonToken = parser.nextToken();
                            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                                if (parser.getCurrentName().equals("Compound_Name")) {
                                    AnnotationHistory annotationHistory = new AnnotationHistory();
                                    parser.nextToken();
                                    annotationHistory.setCompoundName(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setIonSource(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setCompoundSource(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setInstrument(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setPi(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setDataCollector(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setAdduct(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setScan(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setPrecursorMz(parser.getValueAsDouble());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setExactMass(parser.getValueAsDouble());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setCharge(parser.getValueAsInt());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setCasNumber(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setPubmedId(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setSmiles(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setInchi(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setInchiAUX(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setLibraryClass(parser.getValueAsInt());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setSpectrumId(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setIonMode(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setCreateTime(simpleDateFormat.parse(parser.getValueAsString()));
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setTaskId(parser.getValueAsString());
                                    parser.nextToken();
                                    parser.nextToken();
                                    annotationHistory.setUserId(parser.getValueAsString());
                                    annotationHistoryList.add(annotationHistory);
                                }
                            }
                            if (JsonToken.END_ARRAY.equals(jsonToken)) {
                                break;
                            }
                        }
                        spectrumDO.setAnnotationHistoryList(annotationHistoryList);
                        spectrumMap.get(spectrumDO.getLibraryMembership()).add(spectrumDO);
                    }
                }
            }
            log.info("Finish parsing JSON file, start inserting to database");
            for (String libraryName : spectrumMap.keySet()) {
                LibraryDO libraryDO = libraryService.getById(libraryName);
                libraryDO.setSpectrumCount(spectrumMap.get(libraryName).size());
                libraryService.update(libraryDO);
                spectrumService.insert(spectrumMap.get(libraryName), libraryDO.getId());
            }
            log.info("Insert to database success, time cost " + (System.currentTimeMillis() - startTime) / 1000 + "s");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseMsp(String filePath) {
        //read file use buffer
        File file = new File(filePath);
        FileInputStream fis;
        log.info("Start GNPS-format msp file importing, file name: {}", file.getName());

        //create library
        LibraryDO libraryDO = new LibraryDO();
        libraryDO.setName(file.getName().replace(".msp", ""));
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
                    String[] items = line.split(" ");
                    SpectrumDO spectrumDO = new SpectrumDO();
                    spectrumDO.setLibraryId(libraryDO.getId());
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
                        //precursor m/z
                        if (lowerLine.startsWith("precursormz")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setPrecursorMz(Double.parseDouble(items2[1]));
                            }
                        }
                        //precursor type
                        else if (lowerLine.startsWith("precursortype")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setPrecursorAdduct(items2[1]);
                            }
                        }
                        //formula
                        else if (lowerLine.startsWith("formula")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setFormula(items2[1]);
                            }
                        }
                        //ontology
                        else if (lowerLine.startsWith("ontology")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setOntology(items2[1]);
                            }
                        }
                        //inchiKey
                        else if (lowerLine.startsWith("inchikey")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setInChIKey(items2[1]);
                            }
                        }
                        //inchi
                        else if (lowerLine.startsWith("inchi")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setInChI(items2[1]);
                            }
                        }
                        //smiles
                        else if (lowerLine.startsWith("smiles")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setSmiles(items2[1]);
                            }
                        }
                        //retention time
                        else if (lowerLine.startsWith("retentiontime")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                            }
                        }
                        //ionMode
                        else if (lowerLine.startsWith("ionmode")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                if (items2[1].equalsIgnoreCase(IonMode.Positive.getName())) {
                                    spectrumDO.setIonMode(IonMode.Positive.getName());
                                }
                                if (items2[1].equalsIgnoreCase(IonMode.Negative.getName())) {
                                    spectrumDO.setIonMode(IonMode.Negative.getName());
                                }
                            }
                        }
                        //instrumentType
                        else if (lowerLine.startsWith("instrumenttype")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setInstrumentType(items2[1]);
                            }
                        }
                        //instrument
                        else if (lowerLine.startsWith("instrument")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setInstrument(items2[1]);
                            }
                        }
                        //collisionEnergy
                        else if (lowerLine.startsWith("collisionenergy")) {
                            String[] items2 = line.split(" ");
                            Double collisionEnergy = null;
                            if (items2.length > 1) {
                                try {
                                    collisionEnergy = Double.parseDouble(items2[1]);
                                } catch (Exception e) {
                                    log.error("Collision energy parse error: {}", spectrumDO.getCompoundName());
                                }
                                spectrumDO.setCollisionEnergy(collisionEnergy);
                            }
                        }
                        //comment
                        else if (lowerLine.startsWith("comment")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setComment(items2[1]);
                            }
                        }
                        //num peaks
                        else if (lowerLine.startsWith("num peaks")) {
                            List<Double> mzList = new ArrayList<>();
                            List<Double> intensityList = new ArrayList<>();
                            line = br.readLine();
                            while (line != null && !line.isEmpty()) {
                                String[] values = line.split("\t");
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
            log.info("Finish GNPS-format msp file importing, inserted spectrum count: {}", spectrumDOS.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
