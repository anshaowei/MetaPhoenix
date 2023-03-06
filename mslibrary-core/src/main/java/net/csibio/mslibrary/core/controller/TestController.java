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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
//        gnpsParser.parseMsp("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.msp");
//        gnpsParser.parseMgf("/Users/anshaowei/Documents/Metabolomics/library/GNPS/GNPS-LIBRARY.mgf");

        //massbank
//        massBankParser.parseMspEU("/Users/anshaowei/Documents/Metabolomics/library/MassBank/MassBank_NIST.msp");
        massBankParser.parseMspMoNA("/Users/anshaowei/Documents/Metabolomics/library/MoNA-MassBank/MoNA-export-LC-MS_Spectra.msp");
    }

    @RequestMapping("/filter")
    public void filter() {
        //filter all the libraries
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.filter(libraryDO.getId()));

        //basic filter
//        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.basicFilter(libraryDO.getId()));

        //filter on one library
//        String libraryId = "MassBank-MoNA";
//        noiseFilter.filter(libraryId);

        //basic filter on one library
//        String libraryId = "MassBank-MoNA";
//        noiseFilter.basicFilter(libraryId);
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
        for (LibraryDO libraryDO : libraryDOS) {
            if (libraryDO.getId().equals("ALL_GNPS") || libraryDO.getId().equals("MassBank-MoNA")) {
                continue;
            }
            spectrumService.remove(new SpectrumQuery(), libraryDO.getId());
            libraryService.remove(libraryDO.getId());
            mongoTemplate.dropCollection("spectrum" + SymbolConst.DELIMITER + libraryDO.getId());
            log.info("remove done: " + libraryDO.getId());
        }
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

    }

    @RequestMapping("dataExchange")
    public void dataExchange() throws Exception {
        //real data
//        File file = new File("/Users/anshaowei/Downloads/ST001794/ST001794.xlsx");
//        Workbook workbook = new XSSFWorkbook(file);
//        Sheet sheet = workbook.getSheetAt(4);
//        List<SpectrumDO> spectrumDOS = new ArrayList<>();
//        for (int i = 1; i < sheet.getLastRowNum(); i++) {
//            if (i == 1 || i == 831 || i == 54) {
//                continue;
//            }
//            Row row = sheet.getRow(i);
//            if (row.getCell(1).getStringCellValue().contains("lvl 1")) {
//                SpectrumDO spectrumDO = new SpectrumDO();
//                spectrumDO.setLibraryId("ST001794");
//                for (Cell cell : row) {
//                    switch (cell.getColumnIndex()) {
//                        case 0 -> spectrumDO.setCompoundName(cell.getStringCellValue());
//                        case 3 -> spectrumDO.setPrecursorMz(cell.getNumericCellValue());
//                        case 10 -> spectrumDO.setSmiles(cell.getStringCellValue());
//                        case 12 -> {
//                            String values = cell.getStringCellValue();
//                            String[] valueArray = values.split(" ");
//                            double[] mzArray = new double[valueArray.length];
//                            double[] intensityArray = new double[valueArray.length];
//                            for (int j = 0; j < valueArray.length; j++) {
//                                String[] mzAndIntensity = valueArray[j].split(":");
//                                mzArray[j] = Double.parseDouble(mzAndIntensity[0]);
//                                intensityArray[j] = Double.parseDouble(mzAndIntensity[1]);
//                            }
//                            spectrumDO.setMzs(mzArray);
//                            spectrumDO.setInts(intensityArray);
//                        }
//                    }
//                }
//                spectrumDOS.add(spectrumDO);
//            }
//        }
//        spectrumService.insert(spectrumDOS, "ST001794");
//        log.info("import success");

        //sirius data
        siriusParser.parse("sirius", "/Users/anshaowei/Downloads/ProjectSpace");

        //export data
//        exporter.toMsp("ALL_GNPS-integrated", "ALL_GNPS-integrated");
//        exporter.toMsp("MassBank-MoNA", "MassBank-MoNA");
    }

    @RequestMapping("report")
    public void report() {
        //real score distribution sheet by the target-decoy strategy
//        String queryLibraryId = "MassBank-MoNA-integrated";
//        String targetLibraryId = "ALL_GNPS-integrated";
//        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.XYMeta.getName();
//        MethodDO methodDO = new MethodDO();
//        methodDO.setMzTolerance(0.001);
//        methodDO.setPpmForMzTolerance(false);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy.getName());
//        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = fdrControlled.getAllHitsMap(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO);
//        reporter.scoreGraph("score1", hitsMap, 50);

        //recall method
//        String targetLibraryId = "ALL_GNPS-integrated";
//        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.XYMeta.getName();
//        MethodDO methodDO = new MethodDO();
//        methodDO.setMzTolerance(0.001);
//        methodDO.setPpmForMzTolerance(false);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Cosine.getName());
//        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = fdrControlled.getRecallHitsMap(targetLibraryId, decoyLibraryId, methodDO);
//        reporter.scoreGraph("score4", hitsMap, 100);
//        reporter.estimatedPValueGraph("estimatedPValueGraph", hitsMap, 20);

        //compare
//        MethodDO methodDO = new MethodDO();
//        methodDO.setMzTolerance(0.001);
//        methodDO.setPpmForMzTolerance(false);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy.getName());
//        String targetLibraryId = "MassBank-MoNA-integrated";
//        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.XYMeta.getName();
//        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap1 = fdrControlled.getRecallHitsMap(targetLibraryId, decoyLibraryId, methodDO);
//        decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.Entropy_2.getName();
//        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap2 = fdrControlled.getRecallHitsMap(targetLibraryId, decoyLibraryId, methodDO);
//        List<ConcurrentHashMap<SpectrumDO, List<LibraryHit>>> hitsMapList = new ArrayList<>();
//        hitsMapList.add(hitsMap1);
//        hitsMapList.add(hitsMap2);
//        reporter.compareFDRGraph("compareFDRGraph", hitsMapList, 100);

        //simple identification process
        String queryLibraryId = "MassBank-MoNA-integrated";
        String targetLibraryId = "MassBank-MoNA-integrated";
//        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.Entropy_2.getName();
        String decoyLibraryId = "sirius";
        MethodDO methodDO = new MethodDO();
        methodDO.setMzTolerance(0.001);
        methodDO.setPpmForMzTolerance(false);
        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Cosine.getName());
        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = fdrControlled.getAllHitsMap(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO);
        reporter.simpleScoreGraph("simpleScoreGraph", hitsMap, 50, false, false, -30);

        //entropy distribution graph
//        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//        for (LibraryDO libraryDO : libraryDOS) {
//            String libraryId = libraryDO.getId();
//            List<SpectrumDO> spectrumDOS = spectrumService.getAll(new SpectrumQuery(), libraryId);
//            reporter.entropyDistributionGraph(libraryId, libraryId, 50);
//        }
    }

    @RequestMapping("integrate")
    public void integrate() {
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        for (LibraryDO libraryDO : libraryDOS) {
            integrator.integrate(libraryDO.getId());
        }
    }

    @RequestMapping("all")
    public void all() {
        importLibrary();
        filter();
        integrate();
        decoy();
    }

}
