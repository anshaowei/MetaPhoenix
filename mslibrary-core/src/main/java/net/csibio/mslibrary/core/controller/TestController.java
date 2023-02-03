package net.csibio.mslibrary.core.controller;


import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.mslibrary.client.algorithm.search.CommonSearch;
import net.csibio.mslibrary.client.algorithm.similarity.Similarity;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.bean.params.IdentificationParams;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.parser.gnps.GnpsParser;
import net.csibio.mslibrary.client.parser.gnps.MspGNPSParser;
import net.csibio.mslibrary.client.parser.hmdb.SpectrumParser;
import net.csibio.mslibrary.client.parser.massbank.MspMassBankParser;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("test")
@Slf4j
public class TestController {

    @Autowired
    GnpsParser gnpsParser;
    @Autowired
    CompoundService compoundService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;
    @Autowired
    CommonSearch commonSearch;
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

    @RequestMapping("/importLibrary")
    public void importLibrary() {
        log.info("开始执行谱图导入");
//        gnpsParser.parseJSON("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.json");
        mspMassBankParser.parse("/Users/anshaowei/Documents/Metabolomics/library/MassBank/MassBank_NIST.msp");
//        mspGNPSParser.parse("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.msp");
    }

    @RequestMapping("/identify")
    public void identify() {
        log.info("开始进行谱图鉴定");
        String filePath = "/Users/anshaowei/Downloads/(Centroid)_Met_08_Sirius.mgf";
        List<LibraryDO> libraryDOList = libraryService.getAll(new LibraryQuery());
        IdentificationParams identificationParams = new IdentificationParams();
        List<String> libraryIds = new ArrayList<>();
        for (LibraryDO libraryDO : libraryDOList) {
            libraryIds.add(libraryDO.getId());
        }
        identificationParams.setLibraryIds(libraryIds);
        identificationParams.setMzTolerance(0.001);
        identificationParams.setTopN(10);
        identificationParams.setStrategy(1);
        commonSearch.identify(filePath, identificationParams);
        int a = 0;
    }

    @RequestMapping("/recall")
    public void recall() {
        SpectrumQuery spectrumQuery = new SpectrumQuery();
        spectrumQuery.setPrecursorAdduct("[M-H]-");
        List<SpectrumDO> targetSpectrumDOList = spectrumService.getAll(spectrumQuery, "MassBank");
        HashMap<SpectrumDO, List<LibraryHit>> result = new HashMap<>();
        Integer right = 0;
        for (SpectrumDO spectrumDO : targetSpectrumDOList) {
            Double precursorMz = spectrumDO.getPrecursorMz();
            List<LibraryHit> libraryHits = new ArrayList<>();
            SpectrumQuery targetSpectrumQuery = new SpectrumQuery();
            targetSpectrumQuery.setPrecursorMz(precursorMz);
            targetSpectrumQuery.setMzTolerance(0.001);
            List<SpectrumDO> libSpectrumDOList = spectrumService.getAll(targetSpectrumQuery, "MassBank");
            for (SpectrumDO libSpectrumDO : libSpectrumDOList) {
                LibraryHit libraryHit = new LibraryHit();
                libraryHit.setMatchScore(similarity.getDotProduct(spectrumDO.getSpectrum(), libSpectrumDO.getSpectrum(), 0.001));
                libraryHit.setSpectrumId(libSpectrumDO.getId());
                libraryHit.setPrecursorMz(libSpectrumDO.getPrecursorMz());
                libraryHit.setPrecursorAdduct(libSpectrumDO.getPrecursorAdduct());
                libraryHits.add(libraryHit);
            }

            for (LibraryHit libraryHit : libraryHits) {
                if (libraryHit.getSpectrumId().equals(spectrumDO.getId())) {
                    right++;
                    log.info("right:{}", spectrumDO.getSpectrumId());
                    break;
                }
            }

//            libraryHits.sort(Comparator.comparing(LibraryHit::getMatchScore).reversed());
//            if (libraryHits.size() >= 5) {
//                libraryHits = libraryHits.subList(0, 5);
//            }
//            if (libraryHits.get(0).getSpectrumId().equals(spectrumDO.getId())) {
//                right++;
//            } else {
//                log.info("fail_id : {}, fail_score : {}", spectrumDO.getId(), libraryHits.get(0).getMatchScore());
//            }
//            result.put(spectrumDO, libraryHits);
        }
        log.info("total spectrum: {}, right: {}", targetSpectrumDOList.size(), right);
        int a = 0;
    }

    @RequestMapping("decoy")
    public void decoy() {
        spectrumGenerator.optNaive("GNPS");
    }

    @RequestMapping("statistics")
    public void statistics() {
        String libraryId = "MassBank";
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);

        //查看谱图根据smiles分类后的分布情况
        HashMap<String, List<SpectrumDO>> smilesMap = new HashMap<>();
        for (SpectrumDO spectrumDO : spectrumDOS) {
            if (smilesMap.containsKey(spectrumDO.getSmiles())) {
                smilesMap.get(spectrumDO.getSmiles()).add(spectrumDO);
            } else {
                List<SpectrumDO> list = new ArrayList<>();
                list.add(spectrumDO);
                smilesMap.put(spectrumDO.getSmiles(), list);
            }
        }
        int maxSmiles = Integer.MIN_VALUE;
        int minSmiles = Integer.MAX_VALUE;
        int average = 0;
        for (String smiles : smilesMap.keySet()) {
            List<SpectrumDO> list = smilesMap.get(smiles);
            average += list.size();
            if (list.size() > maxSmiles) {
                maxSmiles = list.size();
            }
            if (list.size() < minSmiles) {
                minSmiles = list.size();
            }
        }
        average = average / smilesMap.keySet().size();
        log.info("maxSmiles: " + maxSmiles);
        log.info("minSmiles: " + minSmiles);
        log.info("average: " + average);
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
    }

    @RequestMapping("compare")
    public void compare() {
        long start = System.currentTimeMillis();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId("ST001794");
        int right = 0;
        int exist = 0;
        for (SpectrumDO spectrumDO : spectrumDOS) {
            Double precursorMz = spectrumDO.getPrecursorMz();
            List<SpectrumDO> librarySpectra = spectrumService.getByPrecursorMz(precursorMz, "GNPS");
//            librarySpectra.addAll(spectrumService.getByPrecursorMz(precursorMz, "MassBank"));
            List<LibraryHit> libraryHits = new ArrayList<>();
            for (SpectrumDO librarySpectrum : librarySpectra) {
                if (librarySpectrum.getSmiles().equals(spectrumDO.getSmiles())) {
                    exist++;
                }
//                double score = similarity.getDotProduct(spectrumDO.getSpectrum(), librarySpectrum.getSpectrum(), 0.01);
                double score = similarity.getEntropySimilarity(spectrumDO.getSpectrum(), librarySpectrum.getSpectrum());
                LibraryHit libraryHit = new LibraryHit();
                libraryHit.setSpectrumId(librarySpectrum.getId());
                libraryHit.setSmiles(librarySpectrum.getSmiles());
                libraryHit.setMatchScore(score);
                libraryHits.add(libraryHit);
            }
            //filter by score >0.7
            libraryHits = libraryHits.stream().filter(libraryHit -> libraryHit.getMatchScore() > 0.5).collect(Collectors.toList());
            for (LibraryHit libraryHit : libraryHits) {
                if (libraryHit.getSmiles().equals(spectrumDO.getSmiles())) {
                    right++;
                    break;
                }
            }
        }
        long end = System.currentTimeMillis();
        log.info("time: " + (end - start));
        log.info("right: " + right);
        log.info("exist: " + exist);
        log.info("total: " + spectrumDOS.size());
    }

    @RequestMapping("fdr")
    public void fdr() {
//        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId("ST001794");
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId("GNPS");
        spectrumDOS = spectrumDOS.subList(0, 10000);
        List<LibraryHit> libraryHits = new ArrayList<>();
        String libraryId = "MassBank";
        String decoyLibraryId = libraryId + "-optNaive";
        int incorrect = 0;
        int correct = 0;

        for (SpectrumDO spectrumDO : spectrumDOS) {
            List<SpectrumDO> librarySpectra = spectrumService.getByPrecursorMz(spectrumDO.getPrecursorMz(), libraryId);
            List<SpectrumDO> decoyLibrarySpectra = spectrumService.getByPrecursorMz(spectrumDO.getPrecursorMz(), decoyLibraryId);
            if (librarySpectra.size() == 0 || decoyLibrarySpectra.size() == 0) {
                continue;
            }
            correct++;

            //Library打分
            double maxScore = Double.MIN_VALUE;
            int index = 0;
            for (int i = 0; i < librarySpectra.size(); i++) {
//                double score = similarity.getEntropySimilarity(spectrumDO.getSpectrum(), librarySpectra.get(i).getSpectrum());
                double score = similarity.getDotProduct(spectrumDO.getSpectrum(), librarySpectra.get(i).getSpectrum(), 0.01);
                if (score > maxScore) {
                    maxScore = score;
                    index = i;
                }
                incorrect++;
            }
            LibraryHit libraryHit = new LibraryHit();
            libraryHit.setSpectrumId(librarySpectra.get(index).getId());
            libraryHit.setLibraryName(libraryId);
            libraryHit.setSmiles(librarySpectra.get(index).getSmiles());
            libraryHit.setMatchScore(maxScore);
            libraryHits.add(libraryHit);

            //decoy打分
            maxScore = Double.MIN_VALUE;
            index = 0;
            for (int i = 0; i < decoyLibrarySpectra.size(); i++) {
//                double score = similarity.getEntropySimilarity(spectrumDO.getSpectrum(), decoyLibrarySpectra.get(i).getSpectrum());
                double score = similarity.getDotProduct(spectrumDO.getSpectrum(), decoyLibrarySpectra.get(i).getSpectrum(), 0.01);
                if (score > maxScore) {
                    maxScore = score;
                    index = i;
                }
            }
            LibraryHit decoyLibraryHit = new LibraryHit();
            decoyLibraryHit.setSpectrumId(decoyLibrarySpectra.get(index).getId());
            decoyLibraryHit.setLibraryName(decoyLibraryId);
            libraryHit.setDecoy(true);
            decoyLibraryHit.setSmiles(decoyLibrarySpectra.get(index).getSmiles());
            decoyLibraryHit.setMatchScore(maxScore);
            libraryHits.add(decoyLibraryHit);
        }
        incorrect = incorrect - correct;

        //找到满足FDR条件的分数阈值
        double threshold = 0.0;
        List<List<Double>> scores = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            threshold = i * 0.01;
            double finalThreshold = threshold;
            List<LibraryHit> positiveHits = libraryHits.stream().filter(libraryHit -> libraryHit.getMatchScore() > finalThreshold && !libraryHit.isDecoy()).toList();
            List<LibraryHit> negativeHits = libraryHits.stream().filter(libraryHit -> libraryHit.getMatchScore() > finalThreshold && libraryHit.isDecoy()).toList();
            if (correct + incorrect == 0 || positiveHits.size() == 0) {
                continue;
            }
            double fdr = (double) negativeHits.size() / positiveHits.size() * incorrect / (correct + incorrect);
            List<Double> score = new ArrayList<>();
            score.add(threshold);
            score.add(positiveHits.size() + 0.0);
            score.add(negativeHits.size() + 0.0);
            score.add(fdr + 0.0);
            scores.add(score);
//            if (fdr < 0.05) {
//                log.info("threshold: " + threshold);
//                log.info("positive: " + positiveHits.size());
//                log.info("negative: " + negativeHits.size());
//                break;
//            }
        }
        EasyExcel.write("/Users/anshaowei/Downloads/test.xlsx").sheet("sheet1").doWrite(scores);

        log.info("finish, threshold: " + threshold);
    }

    @RequestMapping("report")
    public void report() {
        String fileName = "test";
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId("ST001794");
        reporter.toMgf(fileName, spectrumDOS);
    }

}
