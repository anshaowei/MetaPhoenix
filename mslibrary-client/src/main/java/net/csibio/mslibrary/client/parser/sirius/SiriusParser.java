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

    public void parse(String filePath) {
        File file = new File(filePath);
        File[] files = file.listFiles();
        assert files != null;
        List<File> fileList = Arrays.asList(files);
        List<SpectrumDO> spectrumDOS = Collections.synchronizedList(new ArrayList<>());
        log.info("start parsing {} files", files.length);
        fileList.parallelStream().forEach(f -> {
            if (!f.getName().equals(".compression") && !f.getName().equals(".version") && !f.getName().equals(".format")) {
                File[] subFiles = f.listFiles();
                assert subFiles != null;
                for (File subFile : subFiles) {
                    if (subFile.getName().equals("spectrum.ms")) {
                        SpectrumDO spectrumDO = parseSpectrum(subFile.getAbsolutePath());
                        if (spectrumDO.getPrecursorMz() != null || spectrumDO.getMzs().length != 0) {
                            spectrumDOS.add(spectrumDO);
                        }
                        log.info("finish parsing {}", subFile.getAbsolutePath());
                    }
                }
            }
        });
        spectrumService.insert(spectrumDOS, "sirius");
    }

    public SpectrumDO parseSpectrum(String decoySpectrumFile) {
        //read file use buffer
        File file = new File(decoySpectrumFile);
        FileInputStream fis = null;
        SpectrumDO spectrumDO = new SpectrumDO();
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
            log.error("error when parsing decoy spectrum file: {}", decoySpectrumFile);
            e.printStackTrace();
        }
        return null;
    }
}
