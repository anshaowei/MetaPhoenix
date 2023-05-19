package net.csibio.mslibrary.client.parser.common;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Component("mgfParser")
@Slf4j
public class MgfParser {

    public List<SpectrumDO> execute(String filePath) {
        //read file use buffer
        File file = new File(filePath);
        FileInputStream fis;
        log.info("Start mgf file parser on {}", file.getName());

        try {
            //fast read of spectra information
            fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new java.io.InputStreamReader(fis));
            String line = null;
            Integer spectrumCount = 0;
            while ((line = br.readLine()) != null) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.startsWith("begin")) {
                    spectrumCount++;
                }
            }
            br.close();
            fis.close();
            log.info("Pre scan: total spectrum count: {}", spectrumCount);

            fis = new FileInputStream(file);
            br = new BufferedReader(new java.io.InputStreamReader(fis));
            List<SpectrumDO> spectrumDOS = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().startsWith("begin ions")) {
                    SpectrumDO spectrumDO = new SpectrumDO();
                    List<Double> mzList = new ArrayList<>();
                    List<Double> intensityList = new ArrayList<>();

                    while (!(line = br.readLine()).toLowerCase().startsWith("end")) {
                        //pepmass
                        if (line.toLowerCase().startsWith("pepmass")) {
                            String value = line.substring("pepmass".length() + 1);
                            spectrumDO.setPrecursorMz(Double.parseDouble(value));
                        }
                        //charge
                        else if (line.toLowerCase().startsWith("charge")) {
                            String value = line.substring("charge".length() + 1);
                            if (value.contains("+")) {
                                value = value.substring(0, value.indexOf("+"));
                                spectrumDO.setCharge(Integer.parseInt(value));
                            } else if (value.contains("-")) {
                                value = value.substring(0, value.indexOf("-"));
                                spectrumDO.setCharge(-Integer.parseInt(value));
                            } else {
                                spectrumDO.setCharge(Integer.parseInt(value));
                            }
                        }
                        //mslevel
                        else if (line.toLowerCase().startsWith("mslevel")) {
                            String value = line.substring("mslevel".length() + 1);
                            spectrumDO.setMsLevel(Integer.parseInt(value));
                        }
                        //ionmode
                        else if (line.toLowerCase().startsWith("ionmode")) {
                            String value = line.substring("ionmode".length() + 1);
                            spectrumDO.setIonMode(value);
                        }
                        //smiles
                        else if (line.toLowerCase().startsWith("smiles")) {
                            String value = line.substring("smiles".length() + 1);
                            spectrumDO.setSmiles(value);
                        }
                        //inchi
                        else if (line.toLowerCase().startsWith("inchi")) {
                            String value = line.substring("inchi".length() + 1);
                            spectrumDO.setInChI(value);
                        }
                        //scan and mz values
                        else if (line.toLowerCase().startsWith("scan")) {
                            while (!(line = br.readLine()).toLowerCase().startsWith("end")) {
                                String[] values = line.split(" ");
                                if (values.length == 2) {
                                    double mz = Double.parseDouble(values[0]);
                                    double intensity = Double.parseDouble(values[1]);
                                    mzList.add(mz);
                                    intensityList.add(intensity);
                                }
                            }
                            break;
                        }
                    }
                    double[] mzArray = new double[mzList.size()];
                    double[] intensityArray = new double[intensityList.size()];
                    for (int i = 0; i < mzList.size(); i++) {
                        mzArray[i] = mzList.get(i);
                        intensityArray[i] = intensityList.get(i);
                    }
                    spectrumDO.setMzs(mzArray);
                    spectrumDO.setInts(intensityArray);
                    //process with charge and pepmass
                    if (spectrumDO.getPrecursorMz() != null) {
                        if (Math.abs(spectrumDO.getCharge()) != 1 && spectrumDO.getPrecursorMz() != 0) {
                            spectrumDO.setPrecursorMz(spectrumDO.getPrecursorMz() / Math.abs(spectrumDO.getCharge()));
                        }
                    }
                    spectrumDOS.add(spectrumDO);
                }
            }
            fis.close();
            br.close();
            return spectrumDOS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
