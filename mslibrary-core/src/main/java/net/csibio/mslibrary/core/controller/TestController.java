package net.csibio.mslibrary.core.controller;


import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.mslibrary.client.algorithm.search.FDRControlled;
import net.csibio.mslibrary.client.constants.enums.DecoyStrategy;
import net.csibio.mslibrary.client.constants.enums.SpectrumMatchMethod;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.filter.NoiseFilter;
import net.csibio.mslibrary.client.parser.gnps.GnpsParser;
import net.csibio.mslibrary.client.parser.hmdb.SpectrumParser;
import net.csibio.mslibrary.client.parser.massbank.MassBankParser;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.core.export.Reporter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("test")
@Slf4j
public class TestController {

    @Autowired
    SpectrumService spectrumService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumParser spectrumParser;
    @Autowired
    MassBankParser massBankParser;
    @Autowired
    SpectrumGenerator spectrumGenerator;
    @Autowired
    Reporter reporter;
    @Autowired
    NoiseFilter noiseFilter;
    @Autowired
    FDRControlled fdrControlled;
    @Autowired
    GnpsParser gnpsParser;
    @Autowired
    MongoTemplate mongoTemplate;

    @RequestMapping("/importLibrary")
    public void importLibrary() {
        gnpsParser.parseJSON("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.json");
//        massBankParser.parseMspEU("/Users/anshaowei/Documents/Metabolomics/library/MassBank/MassBank_NIST.msp");
//        gnpsParser.parseMsp("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.msp");
        massBankParser.parseMspMoNA("/Users/anshaowei/Documents/Metabolomics/library/MoNA-MassBank/MoNA-export-LC-MS_Spectra.msp");
    }

    @RequestMapping("/filter")
    public void filter() {
//        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.filter(libraryDO.getId()));

        //basic filter
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.basicFilter(libraryDO.getId()));
    }

    @RequestMapping("/remove")
    public void remove() {
        //remove all the decoy libraries
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
            for (LibraryDO libraryDO : libraryDOS) {
                spectrumService.remove(new SpectrumQuery(), libraryDO.getId() + SymbolConst.DELIMITER + decoyStrategy.getName());
                mongoTemplate.dropCollection("spectrum" + SymbolConst.DELIMITER + libraryDO.getId() + SymbolConst.DELIMITER + decoyStrategy.getName());
                log.info("remove done: " + libraryDO.getId() + SymbolConst.DELIMITER + decoyStrategy.getName());
            }
        }

        //only remain specific libraries
//        for (LibraryDO libraryDO : libraryDOS) {
//            if (libraryDO.getId().equals("GNPS-NIST14-MATCHES") || libraryDO.getId().equals("MassBank-MoNA")) {
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
        methodDO.setMzTolerance(0.001);
        methodDO.setPpmForMzTolerance(false);
        int repeat = 1;

        //all the strategies on all the libraries
//        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
//            methodDO.setDecoyStrategy(decoyStrategy.getName());
//            List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//            for (LibraryDO libraryDO : libraryDOS) {
//                for (int i = 0; i < repeat; i++) {
//                    spectrumGenerator.execute(libraryDO.getId(), methodDO);
//                }
//            }
//        }

        //all the strategies on one library
        String libraryId = "MassBank-MoNA";
        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
            methodDO.setDecoyStrategy(decoyStrategy.getName());
            for (int i = 0; i < repeat; i++) {
                spectrumGenerator.execute(libraryId, methodDO);
            }
        }
        libraryId = "GNPS-NIST14-MATCHES";
        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
            methodDO.setDecoyStrategy(decoyStrategy.getName());
            for (int i = 0; i < repeat; i++) {
                spectrumGenerator.execute(libraryId, methodDO);
            }
        }

    }

    @RequestMapping("dataImport")
    public void dataImport() throws Exception {
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
                        case 10 -> spectrumDO.setSmiles(cell.getStringCellValue());
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

    @RequestMapping("report")
    public void report() {
        //real score distribution sheet by the target-decoy strategy
//        String queryLibraryId = "GNPS-NIST14-MATCHES";
//        String targetLibraryId = "MassBank-MoNA";
//        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.XYMeta.getName();
//        MethodDO methodDO = new MethodDO();
//        methodDO.setMzTolerance(0.001);
//        methodDO.setPpmForMzTolerance(false);
//        methodDO.setThreshold(0.0);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Cosine.getName());
//        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = fdrControlled.getAllHitsMap(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO);
//        reporter.scoreGraph("score", hitsMap, 100);
//        reporter.estimatedPValueGraph("estimatedPValue", hitsMap, 20);

        //fake identification process
        String queryLibraryId = "GNPS-NIST14-MATCHES";
        String targetLibraryId = "MassBank-MoNA";
        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.Entropy_2.getName();
        MethodDO methodDO = new MethodDO();
        methodDO.setMzTolerance(0.001);
        methodDO.setPpmForMzTolerance(false);
        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy.getName());
        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = fdrControlled.getAllHitsMap(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO);
        reporter.fakeIdentificationGraph("fakeIdentificationGraph", hitsMap, 50);
    }

    @RequestMapping("batchTest")
    public void batchTest() {
        String targetLibraryId = "MassBank-MoNA";
        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.Entropy_2.getName();
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        for (LibraryDO libraryDO : libraryDOS) {
            if (libraryDO.getId().equals(targetLibraryId)) {
                continue;
            }
            String queryLibraryId = libraryDO.getId();
            MethodDO methodDO = new MethodDO();
            methodDO.setMzTolerance(0.001);
            methodDO.setPpmForMzTolerance(false);
            methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Cosine.getName());
            ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = fdrControlled.getAllHitsMap(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO);
            List<LibraryHit> allHits = new ArrayList<>();
            for (SpectrumDO spectrumDO : hitsMap.keySet()) {
                List<LibraryHit> libraryHits = hitsMap.get(spectrumDO);
                allHits.addAll(libraryHits);
            }
            allHits = allHits.stream().filter(libraryHit -> !libraryHit.isDecoy()).collect(Collectors.toList());
            int totalNum = allHits.size();
            allHits = allHits.stream().filter(libraryHit -> libraryHit.getScore() > 0.7).collect(Collectors.toList());
            int proportion = (int) (((double) allHits.size() / totalNum) * 100000);
            libraryDO.setCount(proportion);
            log.info("libraryId {}, proportion: {}", libraryDO.getId(), proportion);
        }
        libraryDOS.sort(Comparator.comparing(LibraryDO::getCount));
        for (LibraryDO libraryDO : libraryDOS) {
            log.info("libraryId {}, proportion: {}", libraryDO.getId(), libraryDO.getCount());
        }
    }

}
