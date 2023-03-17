package net.csibio.mslibrary.client.parser.sirius;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.constants.enums.DecoyStrategy;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@Component("siriusParser")
@Slf4j
public class SiriusParser {

    @Autowired
    SpectrumService spectrumService;

    public void execute(String libraryId, String projectSpace) {

        log.info("start parsing sirius library: {}", libraryId);
        String decoyLibraryId = libraryId + SymbolConst.DELIMITER + DecoyStrategy.FragmentationTree.getName();
        int rawDataPoints = 0, rawSpectraCount = 0;
        List<SpectrumDO> rawSpectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> decoySpectrumDOS = Collections.synchronizedList(new ArrayList<>());
        List<SpectrumDO> filteredSpectrumDOS = Collections.synchronizedList(new ArrayList<>());
        HashMap<String, SpectrumDO> rawSpectraMap = new HashMap<>();
        for (SpectrumDO spectrumDO : rawSpectrumDOS) {
            rawSpectraMap.put(spectrumDO.getId(), spectrumDO);
            rawDataPoints += spectrumDO.getMzs().length;
        }
        rawSpectraCount = rawSpectrumDOS.size();

        File file = new File(projectSpace);
        File[] files = file.listFiles();
        assert files != null;
        List<File> fileList = Arrays.asList(files);
        int leftDataPoints = 0, leftSpectraCount = 0;
        for (File f : fileList) {
            if (!f.getName().equals(".compression") && !f.getName().equals(".version") && !f.getName().equals(".format") && !f.getName().equals(".DS_Store")) {
                File[] subFiles = f.listFiles();
                assert subFiles != null;
                String[] infos = f.getName().split(SymbolConst.UNDERLINE);
                String rawSpectrumId = infos[infos.length - 1];
                SpectrumDO rawSpectrumDO = rawSpectraMap.get(rawSpectrumId);
                if (rawSpectrumDO == null) {
                    log.error("raw spectrum not found: {}", rawSpectrumId);
                    continue;
                }
                SpectrumDO decoySpectrumDO = null;
                SpectrumDO filteredSpectrumDO = null;
                for (File subFile : subFiles) {
                    //noise filtered raw spectra by FragmentationTree annotation
                    if (subFile.getName().equals("spectra")) {
                        File[] subSubFiles = subFile.listFiles();
                        assert subSubFiles != null;
                        for (File subSubFile : subSubFiles) {
                            if (subSubFile.getName().endsWith(".tsv")) {
                                filteredSpectrumDO = parseTsvFile(subSubFile.getAbsolutePath());
                            }
                        }
                    }
                    //decoy spectra by FragmentationTree method
                    if (subFile.getName().equals("decoys")) {
                        File[] subSubFiles = subFile.listFiles();
                        assert subSubFiles != null;
                        for (File subSubFile : subSubFiles) {
                            if (subSubFile.getName().endsWith(".tsv")) {
                                decoySpectrumDO = parseTsvFile(subSubFile.getAbsolutePath());
                            }
                        }
                    }
                }
                if (filteredSpectrumDO == null || decoySpectrumDO == null) {
                    log.error("No filtered or decoy spectrum in {}", f.getName());
                    continue;
                }
                double[] filteredMzs = new double[filteredSpectrumDO.getMzs().length];
                double[] filteredIntensities = new double[filteredSpectrumDO.getInts().length];
                for (int i = 0; i < filteredSpectrumDO.getMzs().length; i++) {
                    filteredMzs[i] = filteredSpectrumDO.getMzs()[i];
                    filteredIntensities[i] = filteredSpectrumDO.getInts()[i];
                }
                rawSpectrumDO.setMzs(filteredMzs);
                rawSpectrumDO.setInts(filteredIntensities);
                filteredSpectrumDOS.add(rawSpectrumDO);
                leftDataPoints += filteredMzs.length;
                leftSpectraCount++;

                decoySpectrumDO.setLibraryId(decoyLibraryId);
                decoySpectrumDO.setPrecursorMz(rawSpectrumDO.getPrecursorMz());
                decoySpectrumDOS.add(decoySpectrumDO);
            }
        }
        spectrumService.remove(new SpectrumQuery(), libraryId);
        spectrumService.remove(new SpectrumQuery(), decoyLibraryId);
        spectrumService.insert(filteredSpectrumDOS, libraryId);
        spectrumService.insert(decoySpectrumDOS, decoyLibraryId);
        log.info("Remove {}% data points and {}% in the library: {}", (double) (rawDataPoints - leftDataPoints) / rawDataPoints * 100, (double) (rawSpectraCount - leftSpectraCount) / rawSpectraCount, libraryId);
        log.info("Finish parsing sirius project space: {}", projectSpace);
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

    //parse tsv format spectrum in the sirius project space
    private SpectrumDO parseTsvFile(String tsvFilePath) {
        SpectrumDO spectrumDO = new SpectrumDO();
        //read file use buffer
        File file = new File(tsvFilePath);
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
            if (mzList.size() == 0 || intensityList.size() == 0) {
                log.error("No data in the tsv file: {}", tsvFilePath);
                return null;
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
            log.error("error when parsing decoy spectrum file: {}", tsvFilePath);
            e.printStackTrace();
        }
        return null;
    }
}

