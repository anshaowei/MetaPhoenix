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
import java.util.concurrent.atomic.AtomicInteger;

@Component("siriusParser")
@Slf4j
public class SiriusParser {

    @Autowired
    SpectrumService spectrumService;

    public void execute(String libraryId, String projectSpace) {

        log.info("start parsing sirius library: {}", libraryId);
        String decoyLibraryId = libraryId + SymbolConst.DELIMITER + DecoyStrategy.FragmentationTree.getName();
        int rawDataPoints = 0;
        List<SpectrumDO> rawSpectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> decoySpectrumDOS = Collections.synchronizedList(new ArrayList<>());
        List<SpectrumDO> filteredSpectrumDOS = Collections.synchronizedList(new ArrayList<>());
        HashMap<String, SpectrumDO> rawSpectraMap = new HashMap<>();
        for (SpectrumDO spectrumDO : rawSpectrumDOS) {
            rawSpectraMap.put(spectrumDO.getId(), spectrumDO);
            rawDataPoints += spectrumDO.getMzs().length;
        }

        File file = new File(projectSpace);
        File[] files = file.listFiles();
        assert files != null;
        List<File> fileList = Arrays.asList(files);
        AtomicInteger filteredDataPoints = new AtomicInteger(0);
        fileList.parallelStream().forEach(f -> {
            if (!f.getName().equals(".compression") && !f.getName().equals(".version") && !f.getName().equals(".format") && !f.getName().equals(".DS_Store")) {
                File[] subFiles = f.listFiles();
                assert subFiles != null;
                SpectrumDO decoySpectrumDO = new SpectrumDO();
                SpectrumDO filteredSpectrumDO = new SpectrumDO();
                String rawSpectrumId = null;
                for (File subFile : subFiles) {
                    //decoy spectra by FragmentationTree method
                    if (subFile.getName().equals("decoys")) {
                        File[] subSubFiles = subFile.listFiles();
                        assert subSubFiles != null;
                        for (File subSubFile : subSubFiles) {
                            if (subSubFile.getName().endsWith(".tsv")) {
                                decoySpectrumDO = parseTsvFile(subSubFile.getAbsolutePath(), decoySpectrumDO);
                            }
                        }
                    }
                    //noise filtered raw spectra by FragmentationTree annotation
                    if (subFile.getName().equals("spectra")) {
                        File[] subSubFiles = subFile.listFiles();
                        assert subSubFiles != null;
                        for (File subSubFile : subSubFiles) {
                            if (subSubFile.getName().endsWith(".tsv")) {
                                filteredSpectrumDO = parseSpectrum(subSubFile.getAbsolutePath(), filteredSpectrumDO);
                            }
                        }
                    }
                    //exact same spectrum as the raw spectrum
                    //used to retrieve the raw spectrum
                    if (subFile.getName().equals("spectrum.ms")) {
                        SpectrumDO tempSpectrumDO = new SpectrumDO();
                        tempSpectrumDO = parseSpectrum(subFile.getAbsolutePath(), tempSpectrumDO);
                        rawSpectrumId = tempSpectrumDO.getComment();
                    }
                }
                if (rawSpectrumId != null && decoySpectrumDO.getMzs() != null && filteredSpectrumDO.getMzs() != null) {
                    SpectrumDO rawSpectrumDO = rawSpectraMap.get(rawSpectrumId);
                    double[] filteredMzs = new double[filteredSpectrumDO.getMzs().length];
                    double[] filteredIntensities = new double[filteredSpectrumDO.getInts().length];
                    for (int i = 0; i < filteredSpectrumDO.getMzs().length; i++) {
                        filteredMzs[i] = filteredSpectrumDO.getMzs()[i];
                        filteredIntensities[i] = filteredSpectrumDO.getInts()[i];
                    }
                    filteredSpectrumDO = rawSpectrumDO;
                    filteredSpectrumDO.setMzs(filteredMzs);
                    filteredSpectrumDO.setInts(filteredIntensities);
                    filteredDataPoints.addAndGet(filteredSpectrumDO.getMzs().length);
                    filteredSpectrumDOS.add(filteredSpectrumDO);

                    decoySpectrumDO.setLibraryId(decoyLibraryId);
                    decoySpectrumDO.setPrecursorMz(rawSpectrumDO.getPrecursorMz());
                    decoySpectrumDOS.add(decoySpectrumDO);
                }
            }
        });
        if (decoySpectrumDOS.size() == 0) {
            log.error("No decoy spectrum exists in the sirius project space: {}", projectSpace);
            return;
        }
        if (filteredSpectrumDOS.size() == 0) {
            log.error("No filtered spectrum exists in the sirius project space: {}", projectSpace);
            return;
        }
        spectrumService.remove(new SpectrumQuery(), libraryId);
        spectrumService.insert(filteredSpectrumDOS, libraryId);
        spectrumService.insert(decoySpectrumDOS, decoyLibraryId);
        int leftDataPoints = filteredDataPoints.get();
        log.info("Remove {}% data points in the library: {}", (double) (rawDataPoints - leftDataPoints) / rawDataPoints, libraryId);
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
    private SpectrumDO parseTsvFile(String tsvFilePath, SpectrumDO spectrumDO) {
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

