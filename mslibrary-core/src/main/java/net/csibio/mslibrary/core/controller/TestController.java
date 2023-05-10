package net.csibio.mslibrary.core.controller;


import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.mslibrary.client.algorithm.integrate.Integrator;
import net.csibio.mslibrary.client.constants.enums.DecoyStrategy;
import net.csibio.mslibrary.client.constants.enums.SpectrumMatchMethod;
import net.csibio.mslibrary.client.domain.bean.spectrum.IonPeak;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.filter.NoiseFilter;
import net.csibio.mslibrary.client.parser.gnps.GnpsParser;
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
import java.util.List;
import java.util.Map;
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
//        gnpsParser.parseMsp("/Users/anshaowei/Documents/Metabolomics/library/GNPS/GNPS-NIST14-MATCHES.msp");
        gnpsParser.parseMsp("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.msp");
//        gnpsParser.parseMgf("/Users/anshaowei/Documents/Metabolomics/library/GNPS/GNPS-LIBRARY.mgf");

        //massbank
//        massBankParser.parseMspEU("/Users/anshaowei/Documents/Metabolomics/library/MassBank/MassBank_NIST.msp");
//        massBankParser.parseMspMoNA("/Users/anshaowei/Documents/Metabolomics/library/MoNA-MassBank/MoNA-export-LC-MS_Spectra.msp");
    }

    @RequestMapping("/filter")
    public void filter() {
        //filter all the libraries
//        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.filter(libraryDO.getId()));

        //basic filter
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.basicFilter(libraryDO.getId()));

        //filter zero data points
//        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
//        libraryDOS.parallelStream().forEach(libraryDO -> noiseFilter.filterZeroPoint(libraryDO.getId()));

        //filter on one library
//        String libraryId = "MassBank-MoNA";
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
            if (decoyStrategy.equals(DecoyStrategy.FragmentationTree)) {
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
//        methodDO.setDecoyStrategy(DecoyStrategy.IonEntropy.getName());
//        spectrumGenerator.execute(libraryId, methodDO);
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
        String libraryId = "ALL_GNPS";
        sirius.execute(libraryId);
        noiseFilter.filterZeroPoint(libraryId);
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
//        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.FragmentationTree.getName();
//        MethodDO methodDO = new MethodDO();
//        methodDO.setPpmForMzTolerance(true);
//        methodDO.setPpm(10);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy);
//        reporter.scoreGraph(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO, 100);

        //simple identification process
//        String queryLibraryId = "MassBank-MoNA";
//        String targetLibraryId = "ALL_GNPS";
//        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.IonEntropy.getName();
//        MethodDO methodDO = new MethodDO();
//        methodDO.setPpmForMzTolerance(true);
//        methodDO.setPpm(10);
//        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy);
//        reporter.simpleScoreGraph(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO, 300, false, true, -30);

        //entropy distribution graph
//        String libraryId = "ALL_GNPS";
//        reporter.entropyDistributionGraph(libraryId, 100);

        //estimate p value graph
        String queryLibraryId = "MassBank-MoNA";
        String targetLibraryId = "ALL_GNPS";
        String decoyLibraryId = targetLibraryId + SymbolConst.DELIMITER + DecoyStrategy.SameMz.getName();
        MethodDO methodDO = new MethodDO();
        methodDO.setPpmForMzTolerance(true);
        methodDO.setPpm(10);
        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy);
        reporter.estimatedPValueGraph(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO, 20);
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

    @RequestMapping("integrate")
    public void integrate() {
        //integrate all the libraries
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        for (LibraryDO libraryDO : libraryDOS) {
            integrator.integrate(libraryDO.getId());
        }

        //integrate one library
//        String libraryId = "MassBank-Europe";
//        integrator.integrate(libraryId);
    }

    @RequestMapping("all")
    public void all() {
        importLibrary();
        filter();
        sirius();
//        decoy();
//        compare();
//        ionEntropy();
    }

    @RequestMapping("ionEntropy")
    public void ionEntropy() {
        String libraryId = "ALL_GNPS";
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        Map<String, List<SpectrumDO>> inchikeyMap = spectrumDOS.stream().collect(Collectors.groupingBy(spectrumDO -> spectrumDO.getInChIKey().split("-")[0]));

        //sort inchikeyMap by the number of spectra descending
        List<Map.Entry<String, List<SpectrumDO>>> inchikeyList = new ArrayList<>(inchikeyMap.entrySet());
        inchikeyList.sort((o1, o2) -> o2.getValue().size() - o1.getValue().size());

        List<IonPeak> ionPeaks = new ArrayList<>();
        for (SpectrumDO spectrumDO : inchikeyList.get(0).getValue()) {
            for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                IonPeak ionPeak = new IonPeak(spectrumDO.getMzs()[i], spectrumDO.getInts()[i]);
                ionPeaks.add(ionPeak);
            }
        }

//        TreeSet<IonPeak> ionPeakSet = new TreeSet<>(ionPeaks);
//        ConcurrentHashMap<IonPeak, Double> ionEntropyMap = new ConcurrentHashMap<>();
//        ionPeakSet.parallelStream().forEach(ionPeak -> {
//            Double mzTolerance = 10 * Constants.PPM * ionPeak.getMz();
//            List<IonPeak> candidates = ionPeaks.stream().filter(ionPeak1 -> Math.abs(ionPeak1.getMz() - ionPeak.getMz()) < mzTolerance).toList();
//            double[] ionIntensities = new double[candidates.size()];
//            for (int i = 0; i < candidates.size(); i++) {
//                ionIntensities[i] = candidates.get(i).getIntensity();
//            }
//            double ionEntropy = Entropy.getEntropy(ionIntensities);
//            ionEntropyMap.put(ionPeak, ionEntropy);
//        });
//
//        //sort ion entropy map by value descending
//        List<Map.Entry<IonPeak, Double>> ionEntropyList = new ArrayList<>(ionEntropyMap.entrySet());
//        Collections.sort(ionEntropyList, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
//        int k = 0;

//            Map<Double, List<IonPeak>> ionPeakMap = ionPeaks.stream().collect(Collectors.groupingBy(IonPeak::getMz));
//            HashMap<Double, Double> ionEntropyMap = new HashMap<>();
//            for (Double mz : ionPeakMap.keySet()) {
//                double[] intensities = new double[ionPeakMap.get(mz).size()];
//                for (int i = 0; i < intensities.length; i++) {
//                    intensities[i] = ionPeakMap.get(mz).get(i).getIntensity();
//                }
//                double ionEntropy = Entropy.getEntropy(intensities);
//                ionEntropyMap.put(mz, ionEntropy);
//            }
//            List<Double> ionMzList = new ArrayList<>(ionEntropyMap.keySet());
//            Collections.sort(ionMzList);

        //mz to ion entropy graph
//            log.info("start export ion entropy graph");
//            List<List<Object>> result = new ArrayList<>();
//            for (int i = 0; i < 100; i++) {
//                final double minMz = i * 10;
//                final double maxMz = (i + 1) * 10;
//                List<Object> row = new ArrayList<>();
//                double avgEntropy = 0;
//                int ionCount = 0;
//                for (Double mz : ionMzList) {
//                    if (mz >= minMz && mz < maxMz) {
//                        ionCount++;
//                        double entropy = ionEntropyMap.get(mz);
//                        avgEntropy += entropy;
//                    }
//                }
//                if (ionCount > 0) {
//                    avgEntropy /= ionCount;
//                }
//                row.add(minMz);
//                row.add(maxMz);
//                row.add(avgEntropy);
//                result.add(row);
//            }
//            List<Object> header = Arrays.asList("minMz", "maxMz", "avgEntropy");
//            result.add(0, header);
//            String fileName = libraryDO.getId();
//            String outputFileName = "/Users/anshaowei/downloads/" + libraryDO.getId() + ".xlsx";
//            EasyExcel.write(outputFileName).sheet(fileName).doWrite(result);
//            log.info("export {} success", outputFileName);

        //zero ion entropy
//            int zeroIonCount = 0;
//            for (Double mz : ionMzList) {
//                if (ionEntropyMap.get(mz) == 0) {
//                    zeroIonCount++;
//                }
//            }
//            log.info("library {} has {} ions, {} of them have zero entropy", libraryDO.getId(), ionMzList.size(), zeroIonCount);

        //ion entropy fraction
//            log.info("start export ion entropy fraction graph");
//            List<List<Object>> result = new ArrayList<>();
//            double maxIonEntropy = 5d;
//            double minIonEntropy = 0d;
//            for (int i = 0; i < 100; i++) {
//                final double minMz = i * 10;
//                final double maxMz = (i + 1) * 10;
//                List<Object> row = new ArrayList<>();
//                double avgEntropy = 0;
//                int ionCount = 0;
//                for (Double mz : ionMzList) {
//                    if (mz >= minMz && mz < maxMz) {
//                        ionCount++;
//                        double entropy = ionEntropyMap.get(mz);
//                        avgEntropy += entropy;
//                    }
//                }
//                if (ionCount > 0) {
//                    avgEntropy /= ionCount;
//                }
//                row.add(minMz);
//                row.add(maxMz);
//                row.add(avgEntropy);
//                result.add(row);
//            }
//            List<Object> header = Arrays.asList("minMz", "maxMz", "avgEntropy");
//            result.add(0, header);
//            String fileName = libraryDO.getId();
//            String outputFileName = "/Users/anshaowei/downloads/" + libraryDO.getId() + ".xlsx";
//            EasyExcel.write(outputFileName).sheet(fileName).doWrite(result);
//            log.info("export {} success", outputFileName);
    }

}
