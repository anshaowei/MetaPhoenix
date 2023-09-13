package net.csibio.mslibrary.core.controller;


import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.aird.enums.MsLevel;
import net.csibio.mslibrary.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.mslibrary.client.algorithm.identification.Identify;
import net.csibio.mslibrary.client.constants.enums.DecoyStrategy;
import net.csibio.mslibrary.client.constants.enums.SpectrumMatchMethod;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.filter.NoiseFilter;
import net.csibio.mslibrary.client.parser.common.MgfParser;
import net.csibio.mslibrary.client.parser.common.MzMLParser;
import net.csibio.mslibrary.client.parser.gnps.GnpsParser;
import net.csibio.mslibrary.client.parser.massbank.MassBankParser;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.core.export.Exporter;
import net.csibio.mslibrary.core.export.Reporter;
import net.csibio.mslibrary.core.sirius.Sirius;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("test")
@Slf4j
public class TestController {

    @Autowired
    SpectrumService spectrumService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    MassBankParser massBankParser;
    @Autowired
    SpectrumGenerator spectrumGenerator;
    @Autowired
    Reporter reporter;
    @Autowired
    NoiseFilter noiseFilter;
    @Autowired
    GnpsParser gnpsParser;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    Exporter exporter;
    @Autowired
    Sirius sirius;
    @Autowired
    MgfParser mgfParser;
    @Autowired
    MzMLParser mzMLParser;
    @Autowired
    Identify identify;

    @RequestMapping("/importLibrary")
    public void importLibrary() {
        //gnps
//        gnpsParser.parseJSON("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.json");
//        gnpsParser.parseMsp("/Users/anshaowei/Documents/Metabolomics/library/GNPS/GNPS-MSMLS.msp");
//        gnpsParser.parseMsp("/Users/anshaowei/Documents/Metabolomics/library/GNPS/GNPS-NIST14-MATCHES.msp");
//        gnpsParser.parseMsp("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.msp");
//        gnpsParser.parseMgf("/Users/anshaowei/Documents/Metabolomics/library/GNPS/GNPS-LIBRARY.mgf");

        //massbank
//        massBankParser.parseMspEU("/Users/anshaowei/Documents/Metabolomics/library/MassBank/MassBank_NIST.msp");
        massBankParser.parseMspMoNA("/Users/anshaowei/Documents/Metabolomics/library/MoNA-MassBank/MoNA-export-LC-MS_Spectra.msp");
//        massBankParser.parseMspEU("/Users/anshaowei/Documents/Metabolomics/library/MassBank/TimeSeris/MassBank_NIST202212.msp", "MassBank-NIST202212");
//        massBankParser.parseMspEU("/Users/anshaowei/Documents/Metabolomics/library/MassBank/TimeSeris/MassBank_NIST202206.msp", "MassBank-NIST202206");
//        massBankParser.parseMspEU("/Users/anshaowei/Documents/Metabolomics/library/MassBank/TimeSeris/MassBank_NIST202306.msp", "MassBank-NIST202306");
//        massBankParser.parseMspEU("/Users/anshaowei/Documents/Metabolomics/library/MassBank/TimeSeris/MassBank_NIST202309.msp", "MassBank-NIST202309");
    }

    @RequestMapping("/filter")
    public void filter() {
        //filter all the libraries
//        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.filter(libraryDO.getId()));

        //basic filter
//        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.basicFilter(libraryDO.getId()));

        //filter zero data points
//        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.filterZeroPoint(libraryDO.getId()));

        //filter on one library
        String libraryId = "MassBank-MoNA";
        noiseFilter.filter(libraryId);

        //basic filter on one library
//        String libraryId = "GNPS-NIST14-MATCHES";
//        noiseFilter.basicFilter(libraryId);
    }

    @RequestMapping("/remove")
    public void remove() {
        //remove all the decoy libraries
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
            if (decoyStrategy.equals(DecoyStrategy.FragmentationTreeBased)) {
                continue;
            }
            for (LibraryDO libraryDO : libraryDOS) {
                spectrumService.remove(new SpectrumQuery(), libraryDO.getId() + SymbolConst.DELIMITER + decoyStrategy.getName());
                mongoTemplate.dropCollection("spectrum" + SymbolConst.DELIMITER + libraryDO.getId() + SymbolConst.DELIMITER + decoyStrategy.getName());
                log.info("remove done: " + libraryDO.getId() + SymbolConst.DELIMITER + decoyStrategy.getName());
            }
        }

        //only remain specific libraries
//        for (LibraryDO libraryDO : libraryDOS) {
//            if (libraryDO.getId().equals("ALL_GNPS") || libraryDO.getId().equals("MassBank-MoNA")) {
//                continue;
//            }
//            spectrumService.remove(new SpectrumQuery(), libraryDO.getId());
//            libraryService.remove(libraryDO.getId());
//            mongoTemplate.dropCollection("spectrum" + SymbolConst.DELIMITER + libraryDO.getId());
//            log.info("remove done: " + libraryDO.getId());
//        }
    }

    @RequestMapping("/decoy")
    public void decoy() {
        MethodDO methodDO = new MethodDO();
        methodDO.setPpm(10);
        methodDO.setPpmForMzTolerance(true);
        int repeat = 1;

        //all the strategies on all the libraries
//        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
//            if (decoyStrategy.equals(DecoyStrategy.FragmentationTree)) {
//                continue;
//            }
//            methodDO.setDecoyStrategy(decoyStrategy.getName());
//            List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//            for (LibraryDO libraryDO : libraryDOS) {
//                for (int i = 0; i < repeat; i++) {
//                    spectrumGenerator.execute(libraryDO.getId(), methodDO);
//                }
//            }
//        }

        //all the strategies on one library
        String libraryId = "ALL_GNPS";
        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
            methodDO.setDecoyStrategy(decoyStrategy.getName());
            for (int i = 0; i < repeat; i++) {
                spectrumGenerator.execute(libraryId, methodDO);
            }
        }
//        methodDO.setDecoyStrategy(DecoyStrategy.IonEntropyBased.getName());
        spectrumGenerator.execute(libraryId, methodDO);
    }

    @RequestMapping("dataExchange")
    public void dataExchange() throws IOException, InvalidFormatException {
        //real data
        File file = new File("/Users/anshaowei/Downloads/ST001794/ST001794.xlsx");
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(4);
        List<SpectrumDO> spectrumDOS = new ArrayList<>();
        for (int i = 1; i < sheet.getLastRowNum(); i++) {
            if (i == 1 || i == 831 || i == 54) {
                continue;
            }
            Row row = sheet.getRow(i);
            if (row.getCell(1).getStringCellValue().contains("lvl 1")) {
                SpectrumDO spectrumDO = new SpectrumDO();
                spectrumDO.setLibraryId("ST001794");
                for (Cell cell : row) {
                    switch (cell.getColumnIndex()) {
                        case 0 -> spectrumDO.setCompoundName(cell.getStringCellValue());
                        case 3 -> spectrumDO.setPrecursorMz(cell.getNumericCellValue());
                        case 9 -> spectrumDO.setInChIKey(cell.getStringCellValue());
                        case 12 -> {
                            String values = cell.getStringCellValue();
                            String[] valueArray = values.split(" ");
                            double[] mzArray = new double[valueArray.length];
                            double[] intensityArray = new double[valueArray.length];
                            for (int j = 0; j < valueArray.length; j++) {
                                String[] mzAndIntensity = valueArray[j].split(":");
                                mzArray[j] = Double.parseDouble(mzAndIntensity[0]);
                                intensityArray[j] = Double.parseDouble(mzAndIntensity[1]);
                            }
                            spectrumDO.setMzs(mzArray);
                            spectrumDO.setInts(intensityArray);
                        }
                    }
                }
                spectrumDOS.add(spectrumDO);
            }
        }
        spectrumService.insert(spectrumDOS, "ST001794");
        log.info("import success");
    }

    @RequestMapping("sirius")
    public void sirius() {
        //sirius process on all the library
//        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//        for (LibraryDO libraryDO : libraryDOS) {
//            sirius.execute(libraryDO.getId());
//            noiseFilter.filterZeroPoint(libraryDO.getId());
//        }

        //sirius process on one library
        String libraryId = "MassBank-MoNA";
        sirius.execute(libraryId);
//        noiseFilter.filterZeroPoint(libraryId);
    }

    @RequestMapping("export")
    public void export() {
        //export all the libraries
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        for (LibraryDO libraryDO : libraryDOS) {
            exporter.toMsp(libraryDO.getId());
        }
    }

    @RequestMapping("report")
    public void report() {
        //real score distribution sheet by the target-decoy strategy
        String queryLibraryId = "MassBank-MoNA";
        String targetLibraryId = "ALL_GNPS";
        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.IonEntropyBased.getName();
        MethodDO methodDO = new MethodDO();
        methodDO.setPpmForMzTolerance(true);
        methodDO.setPpm(10);
        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy);
        reporter.scoreGraph(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO, 100);

        //simple identification process
//        String queryLibraryId = "MassBank-MoNA";
//        String targetLibraryId = "ALL_GNPS";
//        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.IonEntropy.getName();
//        MethodDO methodDO = new MethodDO();
//        methodDO.setPpmForMzTolerance(true);
//        methodDO.setPpm(10);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy);
//        reporter.simpleScoreGraph(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO, 100, false, false, -30);

        //entropy distribution graph
//        String libraryId = "ALL_GNPS";
//        reporter.entropyDistributionGraph(libraryId, 100);

        //estimate p value graph
//        String queryLibraryId = "MassBank-MoNA";
//        String targetLibraryId = "ALL_GNPS";
//        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.IonEntropyBased.getName();
//        MethodDO methodDO = new MethodDO();
//        methodDO.setPpmForMzTolerance(true);
//        methodDO.setPpm(10);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy);
//        reporter.estimatedPValueGraph(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO, 20);

        //ion entropy distribution
//        reporter.ionEntropyDistributionGraph("GNPS-NIST14-MATCHES");
//        reporter.ionEntropyDistributionGraph("ALL_GNPS");
//        reporter.ionEntropyDistributionGraph("MassBank-Europe");
//        reporter.ionEntropyDistributionGraph("MassBank-MoNA");
//
//        reporter.ionEntropyDistributionGraph("MassBank-NIST202212");
//        reporter.ionEntropyDistributionGraph("MassBank-NIST202306");
//        reporter.ionEntropyDistributionGraph("MassBank-NIST202309");
    }

    @RequestMapping("compare")
    public void compare() {
        MethodDO methodDO = new MethodDO();
        methodDO.setPpmForMzTolerance(true);
        methodDO.setPpm(10);
        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy);
        String queryLibraryId = "MassBank-MoNA";
        String targetLibraryId = "ALL_GNPS";

        //compare different spectrum match method
//        reporter.compareSpectrumMatchMethods(queryLibraryId, targetLibraryId, methodDO, 100);

        //compare different decoy strategy
        reporter.compareDecoyStrategy(queryLibraryId, targetLibraryId, methodDO, 100);
    }


    @RequestMapping("identification")
    public void identification() {

        String datasetsPath = "/Users/anshaowei/Downloads/Test";
        String logPath = "/Users/anshaowei/Downloads/log.txt";
        String targetLibraryId = "ALL_GNPS";
        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.IonEntropyBased.getName();
        MethodDO methodDO = new MethodDO();
        methodDO.setPpm(10);
        methodDO.setPpmForMzTolerance(true);
        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy);

        File file = new File(datasetsPath);
        File[] files = file.listFiles();
        try {
            FileWriter fileWriter = new FileWriter(logPath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            assert files != null;

            for (File f : files) {
                if (!f.getName().equals(".DS_Store")) {
                    List<Object> row = new ArrayList<>();
                    List<SpectrumDO> querySpectrumDOS = new ArrayList<>();
                    File[] subFiles = f.listFiles();
                    assert subFiles != null;
                    for (File subFile : subFiles) {
                        if (subFile.getName().endsWith(".mgf") || subFile.getName().endsWith(".MGF")) {
                            List<SpectrumDO> tempSpectrumDOS = mgfParser.execute(subFile.getAbsolutePath());
                            if (tempSpectrumDOS == null || tempSpectrumDOS.size() == 0) {
                                bufferedWriter.write("mgf file is empty: " + subFile.getAbsolutePath() + "\n");
                                log.error("mgf file is empty: " + subFile.getAbsolutePath());
                            } else {
                                querySpectrumDOS.addAll(tempSpectrumDOS);
                            }
                        } else if (subFile.getName().endsWith(".mzML")) {
                            List<SpectrumDO> tempSpectrumDOS = mzMLParser.execute(subFile.getAbsolutePath());
                            if (tempSpectrumDOS == null || tempSpectrumDOS.size() == 0) {
                                bufferedWriter.write("mzML file is empty: " + subFile.getAbsolutePath() + "\n");
                                log.error("mzML file is empty: " + subFile.getAbsolutePath());
                            } else {
                                querySpectrumDOS.addAll(tempSpectrumDOS);
                            }
                        }
                    }

                    //remove low quality spectra
                    int total = querySpectrumDOS.size();
                    querySpectrumDOS.removeIf(spectrumDO -> spectrumDO.getMzs() == null || spectrumDO.getMzs().length == 0 ||
                            spectrumDO.getInts() == null || spectrumDO.getInts().length == 0 ||
                            spectrumDO.getPrecursorMz() == null || spectrumDO.getPrecursorMz() == 0);
                    for (SpectrumDO spectrumDO : querySpectrumDOS) {
                        List<Double> mzs = new ArrayList<>();
                        List<Double> ints = new ArrayList<>();
                        for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                            if (spectrumDO.getInts()[i] == 0d) {
                                continue;
                            }
                            mzs.add(spectrumDO.getMzs()[i]);
                            ints.add(spectrumDO.getInts()[i]);
                        }
                        spectrumDO.setMzs(mzs.stream().mapToDouble(Double::doubleValue).toArray());
                        spectrumDO.setInts(ints.stream().mapToDouble(Double::doubleValue).toArray());
                    }
                    querySpectrumDOS.removeIf(spectrumDO -> spectrumDO.getMsLevel() == null);
                    querySpectrumDOS.removeIf(spectrumDO -> !spectrumDO.getMsLevel().equals(MsLevel.MS2.getCode()));

                    //add querySpectrumID
                    Integer m = 0;
                    for (SpectrumDO spectrumDO : querySpectrumDOS) {
                        spectrumDO.setId(m.toString());
                        m++;
                    }

                    HashMap<String, List<LibraryHit>> result = identify.execute(querySpectrumDOS, targetLibraryId, decoyLibraryId, methodDO, 0.01);
                    int allHits = 0;
                    int matchedHits = 0;
                    for (String queryId : result.keySet()) {
                        List<LibraryHit> libraryHits = result.get(queryId);
                        allHits += libraryHits.size();
                        if (libraryHits.size() > 0) {
                            matchedHits++;
                        }
                    }
                    row.add(f.getName());
                    row.add(total);
                    row.add(querySpectrumDOS.size());
                    row.add(allHits);
                    row.add(matchedHits);
                    allHits = 0;
                    matchedHits = 0;
                    result = identify.execute(querySpectrumDOS, targetLibraryId, decoyLibraryId, methodDO, 0.05);
                    for (String queryId : result.keySet()) {
                        List<LibraryHit> libraryHits = result.get(queryId);
                        allHits += libraryHits.size();
                        if (libraryHits.size() > 0) {
                            matchedHits++;
                        }
                    }
                    row.add(allHits);
                    row.add(matchedHits);
                    allHits = 0;
                    matchedHits = 0;
                    result = identify.execute(querySpectrumDOS, targetLibraryId, decoyLibraryId, methodDO, 0.1);
                    for (String queryId : result.keySet()) {
                        List<LibraryHit> libraryHits = result.get(queryId);
                        allHits += libraryHits.size();
                        if (libraryHits.size() > 0) {
                            matchedHits++;
                        }
                    }
                    row.add(allHits);
                    row.add(matchedHits);

                    //export data sheet
                    List<List<Object>> dataSheet = new ArrayList<>();
                    dataSheet.add(row);
                    List<Object> header = Arrays.asList("Dataset", "totalCount", "QuerySpectra", "AllHits-0.01", "MatchedHits-0.01", "AllHits-0.05", "MatchedHits-0.05", "AllHits-0.1", "MatchedHits-0.1");
                    dataSheet.add(0, header);
                    String outputFilePath = "/Users/anshaowei/Downloads/report/" + f.getName() + ".xlsx";
                    EasyExcel.write(outputFilePath).sheet("identification").doWrite(dataSheet);
                    log.info("export data sheet to " + outputFilePath);
                }
            }
            fileWriter.close();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("all")
    public void all() {
        importLibrary();
        filter();
//        sirius();
//        decoy();
//        identification();
//        report();
//        compare();
//        ionEntropy();
    }

}
