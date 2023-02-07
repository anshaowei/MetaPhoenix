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
import net.csibio.mslibrary.client.filter.NoiseFilter;
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
    @Autowired
    NoiseFilter noiseFilter;

    @RequestMapping("/importLibrary")
    public void importLibrary() {
        log.info("开始执行谱图导入");
//        gnpsParser.parseJSON("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.json");
        mspMassBankParser.parse("/Users/anshaowei/Documents/Metabolomics/library/MassBank/MassBank_NIST.msp");
//        mspGNPSParser.parse("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.msp");
    }

    @RequestMapping("/clean")
    public void clean() {
        noiseFilter.filter("MassBank");
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
        List<SpectrumDO> targetSpectrumDOList = spectrumService.getAllByLibraryId("MassBank");
        HashMap<SpectrumDO, List<LibraryHit>> result = new HashMap<>();
        Integer right = 0;
        for (SpectrumDO spectrumDO : targetSpectrumDOList) {
            Double precursorMz = spectrumDO.getPrecursorMz();
            List<LibraryHit> libraryHits = new ArrayList<>();
            SpectrumQuery targetSpectrumQuery = new SpectrumQuery();
            targetSpectrumQuery.setPrecursorMz(precursorMz);
            targetSpectrumQuery.setMzTolerance(0.001);
            List<SpectrumDO> libSpectrumDOList = spectrumService.getAll(targetSpectrumQuery, "GNPS");
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
//        spectrumGenerator.naive("GNPS");
//        spectrumGenerator.naive("MassBank");
//        spectrumGenerator.spectrumBased("GNPS");
        spectrumGenerator.spectrumBased("MassBank");
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
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId("MassBank");
        Double mzTolerance = 0.01;
        List<LibraryHit> trueHits = new ArrayList<>();
        List<LibraryHit> falseHits = new ArrayList<>();

        for (SpectrumDO spectrumDO : spectrumDOS) {
            Double precursorMz = spectrumDO.getPrecursorMz();
            List<SpectrumDO> librarySpectra = spectrumService.getByPrecursorMz(precursorMz, mzTolerance, "GNPS-naive");
            for (SpectrumDO librarySpectrum : librarySpectra) {
                if (librarySpectrum.getSmiles().equals(spectrumDO.getSmiles()) && Math.abs(librarySpectrum.getPrecursorMz() - spectrumDO.getPrecursorMz()) < 0.1) {
                    double score = similarity.getDotProduct(spectrumDO.getSpectrum(), librarySpectrum.getSpectrum(), 0.01);
//                double score = similarity.getEntropySimilarity(spectrumDO.getSpectrum(), librarySpectrum.getSpectrum());
                    LibraryHit libraryHit = new LibraryHit();
                    libraryHit.setSpectrumId(librarySpectrum.getId());
                    libraryHit.setSmiles(librarySpectrum.getSmiles());
                    libraryHit.setMatchScore(score);
                    trueHits.add(libraryHit);
                } else {
                    double score = similarity.getDotProduct(spectrumDO.getSpectrum(), librarySpectrum.getSpectrum(), 0.01);
//                double score = similarity.getEntropySimilarity(spectrumDO.getSpectrum(), librarySpectrum.getSpectrum());
                    LibraryHit libraryHit = new LibraryHit();
                    libraryHit.setSpectrumId(librarySpectrum.getId());
                    libraryHit.setSmiles(librarySpectrum.getSmiles());
                    libraryHit.setMatchScore(score);
                    falseHits.add(libraryHit);
                }
            }

            // 测定伪谱图的经验贝叶斯分布
//            for(SpectrumDO librarySpectrum : librarySpectra) {
//                double score = similarity.getDotProduct(spectrumDO.getSpectrum(), librarySpectrum.getSpectrum(), 0.01);
//                LibraryHit libraryHit = new LibraryHit();
//                libraryHit.setSpectrumId(librarySpectrum.getId());
//                libraryHit.setSmiles(librarySpectrum.getSmiles());
//                libraryHit.setMatchScore(score);
//                falseHits.add(libraryHit);
//            }
//        }

            double threshold = 0.0;
            List<List<Double>> scores = new ArrayList<>();
            for (int i = 0; i <= 100; i++) {
                threshold = i * 0.01;
                double minValue = threshold;
                double maxValue = threshold + 0.01;
                List<LibraryHit> positiveHits = trueHits.stream().filter(libraryHit -> libraryHit.getMatchScore() > minValue && libraryHit.getMatchScore() <= maxValue).toList();
                List<LibraryHit> negativeHits = falseHits.stream().filter(libraryHit -> libraryHit.getMatchScore() > minValue && libraryHit.getMatchScore() <= maxValue).toList();

                List<Double> score = new ArrayList<>();
                score.add(threshold);
                score.add(positiveHits.size() + 0.0);
                score.add(negativeHits.size() + 0.0);
                scores.add(score);
            }
            EasyExcel.write("/Users/anshaowei/Downloads/compare.xlsx").sheet("sheet1").doWrite(scores);
            long end = System.currentTimeMillis();
            log.info("time: " + (end - start));
        }
    }

    @RequestMapping("fdr")
    public void fdr() {
//        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId("ST001794");
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId("MassBank");
        List<LibraryHit> libraryHits = new ArrayList<>();
        String libraryId = "GNPS";
        String decoyLibraryId = libraryId + "-optNaive";
        Double mzTolerance = 0.001;
        int incorrect = 0;
        int correct = 0;

        for (SpectrumDO spectrumDO : spectrumDOS) {
            List<SpectrumDO> librarySpectra = spectrumService.getByPrecursorMz(spectrumDO.getPrecursorMz(), mzTolerance, libraryId);
            List<SpectrumDO> decoyLibrarySpectra = spectrumService.getByPrecursorMz(spectrumDO.getPrecursorMz(), mzTolerance, decoyLibraryId);
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
            decoyLibraryHit.setDecoy(true);
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
        EasyExcel.write("/Users/anshaowei/Downloads/test_opt.xlsx").sheet("sheet1").doWrite(scores);

        log.info("finish, threshold: " + threshold);
    }

    @RequestMapping("report")
    public void report() {
        String fileName = "test";
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId("ST001794");
        reporter.toMgf(fileName, spectrumDOS);
    }

}
