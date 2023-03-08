package net.csibio.mslibrary.client.parser.sirius;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component("siriusParser")
@Slf4j
public class SiriusParser {
    @Autowired
    SpectrumService spectrumService;

    public void parse(String libraryId, String projectSpace) {
        File file = new File(projectSpace);
        File[] files = file.listFiles();
        assert files != null;
        List<File> fileList = Arrays.asList(files);
        List<SpectrumDO> spectrumDOS = Collections.synchronizedList(new ArrayList<>());
        log.info("start parsing {} files", files.length);
        fileList.parallelStream().forEach(f -> {
            if (!f.getName().equals(".compression") && !f.getName().equals(".version") && !f.getName().equals(".format") && !f.getName().equals(".DS_Store")) {
                File[] subFiles = f.listFiles();
                assert subFiles != null;
                SpectrumDO spectrumDO = new SpectrumDO();
                for (File subFile : subFiles) {
                    if (subFile.getName().equals("decoys")) {
                        File[] subSubFiles = subFile.listFiles();
                        assert subSubFiles != null;
                        for (File subSubFile : subSubFiles) {
                            if (subSubFile.getName().endsWith(".tsv")) {
                                spectrumDO = parseDecoy(subSubFile.getAbsolutePath(), spectrumDO);
                            }
                        }
                    }
                    if (subFile.getName().equals("spectrum.ms")) {
                        SpectrumDO tempSpectrumDO = new SpectrumDO();
                        tempSpectrumDO = parseSpectrum(subFile.getAbsolutePath(), tempSpectrumDO);
                        if (tempSpectrumDO != null) {
                            spectrumDO.setPrecursorMz(tempSpectrumDO.getPrecursorMz());
                            spectrumDO.setSmiles(tempSpectrumDO.getSmiles());
                            spectrumDO.setComment(tempSpectrumDO.getComment());
                        }
                    }
                }
                if (spectrumDO.getComment() != null && spectrumDO.getMzs() != null) {
                    spectrumDO.setLibraryId(libraryId);
                    spectrumDOS.add(spectrumDO);
                }
            }
        });
        if (spectrumDOS.size() == 0) {
            log.error("No decoy spectrum exists in the sirius project space: {}", projectSpace);
            return;
        }
        spectrumService.insert(spectrumDOS, libraryId);
        log.info("Finish parsing sirius generated library");
    }

    //parse spectrum.ms in the sirius project space
    private SpectrumDO parseSpectrum(String spectrumFile, SpectrumDO spectrumDO) {
        //read file use buffer
        File file = new File(spectrumFile);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new java.io.InputStreamReader(fis));
            String line = br.readLine();
            boolean label = false;
            List<Double> mzList = new ArrayList<>();
            List<Double> intensityList = new ArrayList<>();
            while (line != null) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.startsWith("##")) {
                    label = true;
                    String[] items = line.substring(2).split(" ");
                    if (items[0].equals("precursormz")) {
                        spectrumDO.setPrecursorMz(Double.parseDouble(items[1]));
                    }
                    if (items[0].equals("smiles")) {
                        spectrumDO.setSmiles(items[1]);
                    }
                    if (items[0].equals("name")) {
                        spectrumDO.setComment(items[1]);
                    }
                }
                if (label && !line.startsWith(" ") && !line.startsWith("##") && !line.startsWith(">") && !line.equals("")) {
                    String[] items = line.split(" ");
                    mzList.add(Double.parseDouble(items[0]));
                    intensityList.add(Double.parseDouble(items[1]));
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
            fis.close();
            br.close();
            return spectrumDO;
        } catch (Exception e) {
            log.error("error when parsing decoy spectrum file: {}", spectrumFile);
            e.printStackTrace();
        }
        return null;
    }

    //parse decoy spectrum
    private SpectrumDO parseDecoy(String decoyFilePath, SpectrumDO spectrumDO) {
        //read file use buffer
        File file = new File(decoyFilePath);
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new java.io.InputStreamReader(fis));
            String line = br.readLine();
            List<Double> mzList = new ArrayList<>();
            List<Double> intensityList = new ArrayList<>();
            while (line != null) {
                if (!line.startsWith("mz")) {
                    String[] items = line.split("\t");
                    mzList.add(Double.parseDouble(items[0]));
                    intensityList.add(Double.parseDouble(items[1]));
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
            return spectrumDO;
        } catch (Exception e) {
            log.error("error when parsing decoy spectrum file: {}", decoyFilePath);
            e.printStackTrace();
        }
        return null;
    }
}

