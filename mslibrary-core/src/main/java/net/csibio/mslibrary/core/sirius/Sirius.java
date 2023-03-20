package net.csibio.mslibrary.core.sirius;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.constants.enums.DecoyStrategy;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.core.config.VMProperties;
import net.csibio.mslibrary.core.export.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component("sirius")
@Slf4j
public class Sirius {
    @Autowired
    SpectrumService spectrumService;
    @Autowired
    VMProperties vmProperties;
    @Autowired
    Exporter exporter;

    public void execute(String libraryId) {
        String libraryProjectSpace = vmProperties.getSiriusProjectSpace() + File.separator + libraryId;
        String siriusPath = vmProperties.getSiriusPath();

        //export library as standard msp file for sirius
        String outputFileName = vmProperties.getSiriusProjectSpace() + File.separator + libraryId + ".msp";
        Result result = exporter.toMsp(outputFileName, libraryId);
        if (result.isFailed()) {
            log.error("Export library as standard msp file for sirius failed");
            return;
        }

        //create library project space
        File dir = new File(libraryProjectSpace);
        if (!dir.exists()) {
            dir.mkdir();
            log.info("Created sirius project space：" + dir.getName());
        } else {
            deleteDir(dir);
            dir.mkdir();
            log.info("Sirius project space already exists: Removed and recreated");
        }

        //run sirius import and fragmentation tree
        String[] commands = {siriusPath, "-i", outputFileName, "-o", libraryProjectSpace, "tree"};
        if (runCommands(commands) == 0) {
            log.info("Sirius import and calculate fragmentation tree finished");
        } else {
            log.error("Sirius import and calculate fragmentation tree failed");
            return;
        }

        //delete msp file
        File mspFile = new File(outputFileName);
        if (mspFile.exists()) {
            mspFile.delete();
        }

        //generate decoys by FragmentationTree annotation
        commands = new String[]{siriusPath, "-i", libraryProjectSpace, "-o", libraryProjectSpace, "passatutto"};
        if (runCommands(commands) == 0) {
            log.info("Sirius generate decoy spectra finished");
        } else {
            log.error("Sirius generate decoy spectra failed");
            return;
        }
        log.info("Sirius import and generate fragmentation tree decoys finished");

        //get filtered spectra and decoys
        getFilteredSpectra(libraryId, libraryProjectSpace);
        getDecoySpectra(libraryId, libraryProjectSpace);
    }

    public void getFilteredSpectra(String libraryId, String projectSpace) {
        log.info("Start parsing filtered spectra in the sirius project: {}", projectSpace);
        int rawDataPoints = 0, rawSpectraCount = 0;
        List<SpectrumDO> rawSpectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> filteredSpectrumDOS = new ArrayList<>();
        HashMap<String, SpectrumDO> rawSpectraMap = new HashMap<>();
        for (SpectrumDO spectrumDO : rawSpectrumDOS) {
            rawSpectraMap.put(spectrumDO.getId(), spectrumDO);
            rawDataPoints += spectrumDO.getMzs().length;
        }
        rawSpectraCount = rawSpectrumDOS.size();

        File file = new File(projectSpace);
        File[] files = file.listFiles();
        assert files != null;
        int filteredDataPoints = 0, filteredSpectraCount = 0;
        for (File f : files) {
            if (!f.getName().equals(".compression") && !f.getName().equals(".version") && !f.getName().equals(".format") && !f.getName().equals(".DS_Store")) {
                File[] subFiles = f.listFiles();
                assert subFiles != null;
                String[] infos = f.getName().split(SymbolConst.UNDERLINE);
                String rawSpectrumId = infos[infos.length - 1];
                SpectrumDO rawSpectrumDO = rawSpectraMap.get(rawSpectrumId);
                if (rawSpectrumDO == null) {
                    continue;
                }
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
                }
                if (filteredSpectrumDO == null) {
                    log.info("No filtered spectrum in: {}", f.getName());
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
                filteredDataPoints += filteredMzs.length;
                filteredSpectraCount++;
            }
        }
        spectrumService.remove(new SpectrumQuery(), libraryId);
        spectrumService.insert(filteredSpectrumDOS, libraryId);
        log.info("Remove {}% data points and {}% spectra in the library: {}", (double) (rawDataPoints - filteredDataPoints) / rawDataPoints * 100, (double) (rawSpectraCount - filteredSpectraCount) / rawSpectraCount, libraryId);
        log.info("Finish filter library: {} by FragmentationTree method", libraryId);
    }

    public void getDecoySpectra(String libraryId, String projectSpace) {
        log.info("Start parsing decoy spectra in the sirius project: {}", projectSpace);
        String decoyLibraryId = libraryId + SymbolConst.DELIMITER + DecoyStrategy.FragmentationTree;
        List<SpectrumDO> rawSpectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<SpectrumDO> decoySpectrumDOS = new ArrayList<>();
        List<String> deleteIds = new ArrayList<>();
        HashMap<String, SpectrumDO> rawSpectraMap = new HashMap<>();
        for (SpectrumDO spectrumDO : rawSpectrumDOS) {
            rawSpectraMap.put(spectrumDO.getId(), spectrumDO);
        }

        File file = new File(projectSpace);
        File[] files = file.listFiles();
        assert files != null;
        for (File f : files) {
            if (!f.getName().equals(".compression") && !f.getName().equals(".version") && !f.getName().equals(".format") && !f.getName().equals(".DS_Store")) {
                File[] subFiles = f.listFiles();
                assert subFiles != null;
                String[] infos = f.getName().split(SymbolConst.UNDERLINE);
                String rawSpectrumId = infos[infos.length - 1];
                SpectrumDO rawSpectrumDO = rawSpectraMap.get(rawSpectrumId);
                if (rawSpectrumDO == null) {
                    continue;
                }
                SpectrumDO decoySpectrumDO = null;
                for (File subFile : subFiles) {
                    //noise filtered raw spectra by FragmentationTree annotation
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
                if (decoySpectrumDO == null) {
                    deleteIds.add(rawSpectrumDO.getId());
                    log.info("No decoy spectrum in: {}", f.getName());
                    continue;
                }

                decoySpectrumDO.setLibraryId(decoyLibraryId);
                decoySpectrumDO.setPrecursorMz(rawSpectrumDO.getPrecursorMz());
                decoySpectrumDOS.add(decoySpectrumDO);
            }
        }
        spectrumService.remove(new SpectrumQuery(), decoyLibraryId);
        SpectrumQuery spectrumQuery = new SpectrumQuery();
        spectrumQuery.setIds(deleteIds);
        spectrumService.remove(spectrumQuery, libraryId);
        spectrumService.insert(decoySpectrumDOS, decoyLibraryId);
        log.info("Finish insert decoy library: {} by FragmentationTree method", libraryId);
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
                log.error("Error when parsing tsv file: {}", tsvFilePath);
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
            log.error("Error when parsing tsv file: {}", tsvFilePath);
            e.printStackTrace();
        }
        return null;
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

    private void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                deleteDir(file);
            }
        }
        dir.delete();
    }

    private int runCommands(String[] commands) {
        try {
            Process process = Runtime.getRuntime().exec(commands);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
