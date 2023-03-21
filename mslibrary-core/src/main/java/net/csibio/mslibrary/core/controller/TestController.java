package net.csibio.mslibrary.core.controller;


import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.mslibrary.client.algorithm.integrate.Integrator;
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
import net.csibio.mslibrary.client.service.LibraryHitService;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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
    GnpsParser gnpsParser;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    Exporter exporter;
    @Autowired
    Integrator integrator;
    @Autowired
    LibraryHitService libraryHitService;
    @Autowired
    Sirius sirius;

    @RequestMapping("/importLibrary")
    public void importLibrary() {
        //gnps
//        gnpsParser.parseJSON("/Users/anshaowei/Documents/Metabolomics/library/GNPS/GNPS-LIBRARY.json");
        gnpsParser.parseMsp("/Users/anshaowei/Documents/Metabolomics/library/GNPS/GNPS-NIST14-MATCHES.msp");
//        gnpsParser.parseMsp("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.msp");
//        gnpsParser.parseMgf("/Users/anshaowei/Documents/Metabolomics/library/GNPS/GNPS-LIBRARY.mgf");

        //massbank
        massBankParser.parseMspEU("/Users/anshaowei/Documents/Metabolomics/library/MassBank/MassBank_NIST.msp");
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
//        String libraryId = "MassBank-Europe";
//        noiseFilter.filter(libraryId);

        //basic filter on one library
//        String libraryId = "GNPS-NIST14-MATCHES";
//        noiseFilter.basicFilter(libraryId);
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
        methodDO.setPpm(10);
        methodDO.setPpmForMzTolerance(true);
        int repeat = 1;

        //all the strategies on all the libraries
        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
            if (decoyStrategy.equals(DecoyStrategy.FragmentationTree)) {
                continue;
            }
            methodDO.setDecoyStrategy(decoyStrategy.getName());
            List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
            for (LibraryDO libraryDO : libraryDOS) {
                for (int i = 0; i < repeat; i++) {
                    spectrumGenerator.execute(libraryDO.getId(), methodDO);
                }
            }
        }

        //all the strategies on one library
//        String libraryId = "MassBank-Europe";
//        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
//            if (decoyStrategy.equals(DecoyStrategy.FragmentationTree)) {
//                continue;
//            }
//            methodDO.setDecoyStrategy(decoyStrategy.getName());
//            for (int i = 0; i < repeat; i++) {
//                spectrumGenerator.execute(libraryId, methodDO);
//            }
//        }

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

    @RequestMapping("sirius")
    public void sirius() {
        //sirius process
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        for (LibraryDO libraryDO : libraryDOS) {
            sirius.execute(libraryDO.getId());
        }
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
//        String queryLibraryId = "MassBank-MoNA";
//        String targetLibraryId = "ALL_GNPS";
//        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.XYMeta.getName();
//        MethodDO methodDO = new MethodDO();
//        methodDO.setPpmForMzTolerance(true);
//        methodDO.setPpm(10);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy);
//        List<SpectrumDO> querySpectrumDOS = spectrumService.getAll(new SpectrumQuery(), queryLibraryId);
//        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = libraryHitService.getTargetDecoyHitsMap(querySpectrumDOS, targetLibraryId, decoyLibraryId, methodDO);
//        reporter.scoreGraph("score", hitsMap, 50);

        //simple identification process
//        String queryLibraryId = "GNPS-NIST14-MATCHES";
//        String targetLibraryId = "MassBank-MoNA";
//        MethodDO methodDO = new MethodDO();
//        methodDO.setPpmForMzTolerance(true);
//        methodDO.setPpm(10);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Cosine);
//        List<SpectrumDO> querySpectrumDOS = spectrumService.getAll(new SpectrumQuery(), queryLibraryId);
//        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = libraryHitService.getTargetDecoyHitsMap(querySpectrumDOS, targetLibraryId, null, methodDO);
//        reporter.simpleScoreGraph("simpleScoreGraph", hitsMap, 50, true, false, -30);

        //entropy distribution graph
        String libraryId = "MassBank-MoNA";
        Set<String> names = mongoTemplate.getCollectionNames();
        HashMap<String, List<SpectrumDO>> idSpectraMap = new HashMap<>();
        for (String name : names) {
            if (!name.contains(libraryId)) {
                continue;
            }
            List<SpectrumDO> spectrumDOS = mongoTemplate.findAll(SpectrumDO.class, name);
            if (name.equals("spectrum" + SymbolConst.DELIMITER + libraryId)) {
                idSpectraMap.put("raw", spectrumDOS);
            } else {
                name = name.replace("spectrum-" + libraryId + SymbolConst.DELIMITER, "");
                idSpectraMap.put(name, spectrumDOS);
            }
        }
        reporter.entropyDistributionGraph("entropyDistributionGraph", idSpectraMap, 50);
    }

    @RequestMapping("compare")
    public void compare() {
        MethodDO methodDO = new MethodDO();
        methodDO.setPpmForMzTolerance(true);
        methodDO.setPpm(10);
        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy);
        String queryLibraryId = "GNPS-NIST14-MATCHES";
        List<SpectrumDO> querySpectrumDOS = spectrumService.getAllByLibraryId(queryLibraryId);
        String targetLibraryId = "MassBank-Europe";

        //compare different spectrum match method
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Cosine.getName());
//        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap1 = fdrControlled.getAllHitsMap(queryLibraryId, targetLibraryId, null, methodDO);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy.getName());
//        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap2 = fdrControlled.getAllHitsMap(queryLibraryId, targetLibraryId, null, methodDO);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Unweighted_Entropy.getName());
//        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap3 = fdrControlled.getAllHitsMap(queryLibraryId, targetLibraryId, null, methodDO);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.MetaPro.getName());
//        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap4 = fdrControlled.getAllHitsMap(queryLibraryId, targetLibraryId, null, methodDO);
//        List<ConcurrentHashMap<SpectrumDO, List<LibraryHit>>> hitsMapList = new ArrayList<>();
//        hitsMapList.add(hitsMap1);
//        hitsMapList.add(hitsMap2);
//        hitsMapList.add(hitsMap3);
//        hitsMapList.add(hitsMap4);
//        reporter.compareSpectrumMatchMethods("compareSpectrumMatchMethods", hitsMapList, 50);

        //compare different decoy strategy
        HashMap<String, ConcurrentHashMap<SpectrumDO, List<LibraryHit>>> hitsMapMap = new HashMap<>();
        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
            String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + decoyStrategy.getName();
            if (!mongoTemplate.collectionExists("spectrum" + SymbolConst.DELIMITER + decoyLibraryId)) {
                continue;
            }
            ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = libraryHitService.getTargetDecoyHitsMap(querySpectrumDOS, targetLibraryId, decoyLibraryId, methodDO);
            hitsMapMap.put(decoyStrategy.getName(), hitsMap);
        }
        reporter.compareDecoyStrategy("compareDecoyStrategy", hitsMapMap, 100);
    }

    @RequestMapping("integrate")
    public void integrate() {
        //integrate all the libraries
//        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//        for (LibraryDO libraryDO : libraryDOS) {
//            integrator.integrate(libraryDO.getId());
//        }

        //integrate one library
//        String libraryId = "MassBank-Europe";
//        integrator.integrate(libraryId);

        //test InChIKey combination
//        for (LibraryDO libraryDO : libraryService.getAll(new LibraryQuery())) {
//            String libraryId = libraryDO.getId();
//            List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
//            int k = spectrumDOS.stream().collect(Collectors.groupingBy(spectrumDO -> spectrumDO.getInChIKey().split(SymbolConst.DELIMITER)[0])).keySet().size();
//            log.info("libraryId: {}, size: {}, k: {}", libraryId, spectrumDOS.size(), k);
//        }
    }

    @RequestMapping("all")
    public void all() {
        importLibrary();
        filter();
//        sirius();
//        decoy();
    }
}
