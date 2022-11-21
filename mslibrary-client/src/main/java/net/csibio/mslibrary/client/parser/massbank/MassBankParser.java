package net.csibio.mslibrary.client.parser.massbank;

import net.csibio.mslibrary.client.constants.enums.IonMode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MassBankParser {

    public Result parse(String filePath) {
        //read file use buffer
        File file = new File(filePath);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new java.io.InputStreamReader(fis));
            String line = null;
            List<SpectrumDO> spectrumDOS = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.startsWith("name")) {
                    String[] items = line.split(" ");
                    SpectrumDO spectrumDO = new SpectrumDO();
                    if (items.length > 1) {
                        spectrumDO.setCompoundName(items[1]);
                    }
                    while ((line = br.readLine()) != null) {
                        lowerLine = line.toLowerCase();
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

                            }
                        }
                        //formula
                        else if (lowerLine.startsWith("formula")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setFormulaInchi(items2[1]);
                            }
                        }
                        //ontology
                        else if (lowerLine.startsWith("ontology")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                            }
                        }
                        //inchiKey
                        else if (lowerLine.startsWith("inchikey")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setInchiKeyInchi(items2[1]);
                            }
                        }
                        //inchi
                        else if (lowerLine.startsWith("inchi")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                                spectrumDO.setInchI(items2[1]);
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
                            if (items2.length > 1) {
                            }
                        }
                        //comment
                        else if (lowerLine.startsWith("comment")) {
                            String[] items2 = line.split(" ");
                            if (items2.length > 1) {
                            }
                        }
                        //num peaks
//                        else if (lowerLine.startsWith("num peaks")) {
//                            List<Double> mzList = new ArrayList<>();
//                            List<Double> intensityList = new ArrayList<>();
//                            while (!Objects.equals(line = br.readLine(), "\n") && line != null) {
//                                String lowerLine2 = line.toLowerCase();
//                                if (lowerLine2.startsWith("name")) {
//                                    break;
//                                }
//                                String[] values = line.split(" ");
//                                if (values.length > 1) {
//                                    double mz = Double.parseDouble(values[0]);
//                                    double intensity = Double.parseDouble(values[1]);
//                                    mzList.add(mz);
//                                    intensityList.add(intensity);
//                                }
//                            }
//                            //convert mzList to Array
//                            double[] mzArray = new double[mzList.size()];
//                            for (int i = 0; i < mzList.size(); i++) {
//                                mzArray[i] = mzList.get(i);
//                            }
//                            //convert intensityList to Array
//                            double[] intensityArray = new double[intensityList.size()];
//                            for (int i = 0; i < intensityList.size(); i++) {
//                                intensityArray[i] = intensityList.get(i);
//                            }
//                            spectrumDO.setMzs(mzArray);
//                            spectrumDO.setInts(intensityArray);
//                        }
                        else if (lowerLine.startsWith("name")) {
                            break;
                        }
                    }
                    spectrumDOS.add(spectrumDO);
                }
            }
            int a = 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Result(true);
    }

}
