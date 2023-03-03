package net.csibio.mslibrary.core.controller;


import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.mslibrary.client.algorithm.integrate.Integrator;
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
import net.csibio.mslibrary.client.parser.sirius.SiriusParser;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.core.export.Exporter;
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    @Autowired
    Exporter exporter;
    @Autowired
    SiriusParser siriusParser;
    @Autowired
    Integrator integrator;

    @RequestMapping("/importLibrary")
    public void importLibrary() {
        //gnps
//        gnpsParser.parseJSON("/Users/anshaowei/Documents/Metabolomics/library/GNPS/GNPS-LIBRARY.json");
//        gnpsParser.parseMsp("/Users/anshaowei/Documents/Metabolomics/library/GNPS/GNPS-NIST14-MATCHES.msp");
        gnpsParser.parseMsp("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.msp");
//        gnpsParser.parseMgf("/Users/anshaowei/Documents/Metabolomics/library/GNPS/GNPS-LIBRARY.mgf");

        //massbank
//        massBankParser.parseMspEU("/Users/anshaowei/Documents/Metabolomics/library/MassBank/MassBank_NIST.msp");
        massBankParser.parseMspMoNA("/Users/anshaowei/Documents/Metabolomics/library/MoNA-MassBank/MoNA-export-LC-MS_Spectra.msp");

        //sirius
//        siriusParser.parse("/Users/anshaowei/Documents/Metabolomics/library/Decoy");
    }

    @RequestMapping("/filter")
    public void filter() {
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.filter(libraryDO.getId()));

        //basic filter
//        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.basicFilter(libraryDO.getId()));
    }

    @RequestMapping("compound")
    public void compound() {
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.filter(libraryDO.getId()));
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
        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
            if (decoyStrategy.equals(DecoyStrategy.Entropy_2) || decoyStrategy.equals(DecoyStrategy.XYMeta)) {
                methodDO.setDecoyStrategy(decoyStrategy.getName());
                List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
                for (LibraryDO libraryDO : libraryDOS) {
                    for (int i = 0; i < repeat; i++) {
                        spectrumGenerator.execute(libraryDO.getId(), methodDO);
                    }
                }
            }
        }

        //all the strategies on one library
//        String libraryId = "MassBank-MoNA";
//        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
//            methodDO.setDecoyStrategy(decoyStrategy.getName());
//            for (int i = 0; i < repeat; i++) {
//                spectrumGenerator.execute(libraryId, methodDO);
//            }
//        }
//        libraryId = "GNPS-NIST14-MATCHES";
//        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
//            methodDO.setDecoyStrategy(decoyStrategy.getName());
//            for (int i = 0; i < repeat; i++) {
//                spectrumGenerator.execute(libraryId, methodDO);
//            }
//        }

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
        String queryLibraryId = "MassBank-MoNA-merged";
        String targetLibraryId = "MassBank-MoNA-merged";
        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.XYMeta.getName();
        MethodDO methodDO = new MethodDO();
        methodDO.setMzTolerance(0.001);
        methodDO.setPpmForMzTolerance(false);
        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy.getName());
        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = fdrControlled.getRecallHitsMap(targetLibraryId, decoyLibraryId, methodDO);
        reporter.scoreGraph("score", hitsMap, 50);
        reporter.estimatedPValueGraph("estimatedPValue", hitsMap, 20);

        //simple identification process
//        String queryLibraryId = "MassBank-MoNA-merged";
//        String targetLibraryId = "ALL_GNPS-merged";
//        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.Entropy_2.getName();
//        MethodDO methodDO = new MethodDO();
//        methodDO.setMzTolerance(0.001);
//        methodDO.setPpmForMzTolerance(false);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Cosine.getName());
//        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = fdrControlled.getAllHitsMap(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO);
//        reporter.simpleScoreGraph("simpleScoreGraph", hitsMap, 50, false, false, -30);

        //entropy distribution graph
//        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//        for (LibraryDO libraryDO : libraryDOS) {
//            String libraryId = libraryDO.getId();
//            List<SpectrumDO> spectrumDOS = spectrumService.getAll(new SpectrumQuery(), libraryId);
//            reporter.entropyDistributionGraph(libraryId, libraryId, 50);
//        }
    }

    @RequestMapping("export")
    public void export() {
        exporter.toMsp("ALL_GNPS_Filtered", "ALL_GNPS");
        exporter.toMsp("MassBank-MoNA_Filtered", "MassBank-MoNA");
    }

    @RequestMapping("integrate")
    public void integrate() {
        String libraryId = "MassBank-MoNA";
        integrator.integrate(libraryId);
    }

}
