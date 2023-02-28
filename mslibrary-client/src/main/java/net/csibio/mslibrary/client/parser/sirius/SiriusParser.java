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
import java.util.List;

@Component("siriusParser")
@Slf4j
public class SiriusParser {
    @Autowired
    SpectrumService spectrumService;

    public void parse(String filePath) {

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
                    if (items[0].equals("name")) {
                        spectrumDO.setCompoundName(items[1]);
                    }
                    if (items[0].equals("ionmode")) {
                        spectrumDO.setIonMode(items[1]);
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
            return spectrumDO;
        } catch (Exception e) {
            log.error("error when parsing decoy spectrum file: {}", decoySpectrumFile);
            e.printStackTrace();
        }
        return null;
    }
}
