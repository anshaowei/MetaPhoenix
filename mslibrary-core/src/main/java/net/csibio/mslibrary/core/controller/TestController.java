package net.csibio.mslibrary.core.controller;


import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.mslibrary.client.algorithm.search.FDRControlled;
import net.csibio.mslibrary.client.algorithm.similarity.Similarity;
import net.csibio.mslibrary.client.constants.enums.DecoyStrategy;
import net.csibio.mslibrary.client.constants.enums.SpectrumMatchMethod;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.filter.NoiseFilter;
import net.csibio.mslibrary.client.parser.gnps.MspGNPSParser;
import net.csibio.mslibrary.client.parser.hmdb.SpectrumParser;
import net.csibio.mslibrary.client.parser.massbank.MspMassBankParser;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.core.export.Reporter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("test")
@Slf4j
public class TestController {

    @Autowired
    SpectrumService spectrumService;
    @Autowired
    SpectrumParser spectrumParser;
    @Autowired
    MspMassBankParser mspMassBankParser;
    @Autowired
    Similarity similarity;
    @Autowired
    MspGNPSParser mspGNPSParser;
    @Autowired
    SpectrumGenerator spectrumGenerator;
    @Autowired
    Reporter reporter;
    @Autowired
    NoiseFilter noiseFilter;
    @Autowired
    FDRControlled fdrControlled;

    @RequestMapping("/importLibrary")
    public void importLibrary() {
//        gnpsParser.parseJSON("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.json");
        mspMassBankParser.parse("/Users/anshaowei/Documents/Metabolomics/library/MassBank/MassBank_NIST.msp");
        mspGNPSParser.parse("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.msp");
    }

    @RequestMapping("/filter")
    public void clean() {
        noiseFilter.filter("GNPS");
        noiseFilter.filter("MassBank");
    }

    @RequestMapping("/remove")
    public void remove() {
        String libraryId = "MassBank";
        for (DecoyStrategy decoyStrategy : DecoyStrategy.values()) {
            spectrumService.remove(new SpectrumQuery(), libraryId + "_" + decoyStrategy.getName());
        }
        log.info("remove done");
    }

    @RequestMapping("/decoy")
    public void decoy() {
        MethodDO methodDO = new MethodDO();
        methodDO.setMzTolerance(0.001);
        methodDO.setPpmForMzTolerance(false);
        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Cosine.getName());
        methodDO.setDecoyStrategy(DecoyStrategy.XYMeta.getName());
        spectrumGenerator.execute("GNPS", methodDO);
        spectrumGenerator.execute("MassBank", methodDO);

//        methodDO.setDecoyStrategy(DecoyStrategy.Naive.getName());
//        spectrumGenerator.execute("GNPS", methodDO);
//        spectrumGenerator.execute("MassBank", methodDO);

//        methodDO.setDecoyStrategy(DecoyStrategy.SpectrumBased.getName());
//        spectrumGenerator.execute("GNPS", methodDO);
//        spectrumGenerator.execute("MassBank", methodDO);
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
        String queryLibraryId = "MassBank";
        String targetLibraryId = "GNPS";
        String decoyLibraryId = targetLibraryId + "_XYMeta";
        MethodDO methodDO = new MethodDO();
        methodDO.setMzTolerance(0.001);
        methodDO.setPpmForMzTolerance(false);
        methodDO.setThreshold(0.0);
        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Cosine.getName());

        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = fdrControlled.getAllHitsMap(queryLibraryId, targetLibraryId, decoyLibraryId, methodDO);
        reporter.scoreGraph("score", hitsMap, 100);
//        reporter.estimatedPValueGraph("estimatedPValue", hitsMap, 20);
    }

}
