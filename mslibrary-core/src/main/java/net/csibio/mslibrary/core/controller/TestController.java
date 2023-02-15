package net.csibio.mslibrary.core.controller;


import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.mslibrary.client.algorithm.similarity.Similarity;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
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
import java.util.concurrent.atomic.AtomicInteger;

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
                libraryHit.setScore(similarity.getDotProduct(spectrumDO.getSpectrum(), libSpectrumDO.getSpectrum(), 0.001));
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
//        spectrumGenerator.optNaive("GNPS");
//        spectrumGenerator.spectrumBased("GNPS");
//        spectrumGenerator.spectrumBased("MassBank");
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

    /**
     * 此图为Estimated p-value to identification distribution
     * 参考文献：2017, Scheubert et al. Figure 2
     */
    @RequestMapping("distribution")
    public void distribution() {
        long start = System.currentTimeMillis();
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId("MassBank");
        Double mzTolerance = 0.01;
        List<LibraryHit> trueHits = Collections.synchronizedList(new ArrayList<>());
        List<LibraryHit> falseHits = Collections.synchronizedList(new ArrayList<>());

        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            List<LibraryHit> libraryHits = new ArrayList<>();
            List<SpectrumDO> librarySpectra = spectrumService.getByPrecursorMz(spectrumDO.getPrecursorMz(), mzTolerance, "GNPS");
            List<SpectrumDO> decoySpectra = spectrumService.getByPrecursorMz(spectrumDO.getPrecursorMz(), mzTolerance, "GNPS-optNaive");
            for (SpectrumDO librarySpectrum : librarySpectra) {
//                double score = similarity.getDotProduct(spectrumDO.getSpectrum(), librarySpectrum.getSpectrum(), mzTolerance);
                double score = similarity.getEntropySimilarity(spectrumDO.getSpectrum(), librarySpectrum.getSpectrum(), mzTolerance);
                LibraryHit libraryHit = new LibraryHit();
                libraryHit.setSpectrumId(librarySpectrum.getId());
                libraryHit.setSmiles(librarySpectrum.getSmiles());
                libraryHit.setScore(score);
                libraryHits.add(libraryHit);
            }
            if (libraryHits.size() > 1) {
                libraryHits.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                trueHits.add(libraryHits.get(0));
            }
            libraryHits = new ArrayList<>();
            for (SpectrumDO decoySpectrum : decoySpectra) {
//                double score = similarity.getDotProduct(spectrumDO.getSpectrum(), decoySpectrum.getSpectrum(), mzTolerance);
                double score = similarity.getEntropySimilarity(spectrumDO.getSpectrum(), decoySpectrum.getSpectrum(), mzTolerance);
                LibraryHit libraryHit = new LibraryHit();
                libraryHit.setSpectrumId(decoySpectrum.getId());
                libraryHit.setSmiles(decoySpectrum.getSmiles());
                libraryHit.setScore(score);
                libraryHit.setDecoy(true);
                libraryHits.add(libraryHit);
            }
            if (libraryHits.size() > 1) {
                libraryHits.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                falseHits.add(libraryHits.get(0));
            }
        });

        double threshold = 0.0;
        List<List<Object>> scores = new ArrayList<>();

        //找到20个estimated p-value阈值下的分数值
        List<Double> fdrs = new ArrayList<>();
        List<Double> thresholds = new ArrayList<>();
        for (int i = 20; i > 0; i--) {
            fdrs.add(i * 0.05);
        }
        int count = 0;
        for (int i = 0; i < 10000; i++) {
            threshold = i * 0.001;
            final double minValue = threshold;
            List<LibraryHit> positiveHits = trueHits.stream().filter(libraryHit -> libraryHit.getScore() > minValue).toList();
            List<LibraryHit> negativeHits = falseHits.stream().filter(libraryHit -> libraryHit.getScore() > minValue).toList();
            double pValue = (double) negativeHits.size() / positiveHits.size();
            if (count < fdrs.size()) {
                if (pValue < fdrs.get(count)) {
                    thresholds.add(threshold);
                    count++;
                }
            } else {
                break;
            }
        }

        //作图
        scores.add(new ArrayList<>(Arrays.asList("Estimated-pValue", "true", "false", "true&false")));
        for (int i = thresholds.size() - 1; i >= 0; i--) {
            double minValue = thresholds.get(i);
            double maxValue = 0.0;
            if (i == thresholds.size() - 1) {
                maxValue = 1.0;
            } else {
                maxValue = thresholds.get(i + 1);
            }
            final double finalMaxValue = maxValue;
            List<LibraryHit> positiveHits = trueHits.stream().filter(libraryHit -> libraryHit.getScore() > minValue).toList();
            List<LibraryHit> negativeHits = falseHits.stream().filter(libraryHit -> libraryHit.getScore() > minValue).toList();
            double pValue = (double) negativeHits.size() / positiveHits.size();
            positiveHits = positiveHits.stream().filter(libraryHit -> libraryHit.getScore() <= finalMaxValue).toList();
            negativeHits = negativeHits.stream().filter(libraryHit -> libraryHit.getScore() <= finalMaxValue).toList();
            List<Object> score = new ArrayList<>();
            score.add(pValue);
            score.add((double) positiveHits.size() / trueHits.size());
            score.add((double) negativeHits.size() / falseHits.size());
            score.add((double) (positiveHits.size() + negativeHits.size()) / (trueHits.size() + falseHits.size()));
            scores.add(score);
        }
        EasyExcel.write("/Users/anshaowei/Downloads/scoreGraph.xlsx").sheet("sheet1").doWrite(scores);
        long end = System.currentTimeMillis();
        log.info("time: " + (end - start));
    }

    @RequestMapping("fdr")
    public void fdr() {
//        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId("ST001794");
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId("MassBank");
        List<LibraryHit> libraryHits = Collections.synchronizedList(new ArrayList<>());
        String libraryId = "GNPS";
        String decoyLibraryId = libraryId + "-spectrumBased";
        Double mzTolerance = 0.001;
        AtomicInteger incorrect = new AtomicInteger();
        AtomicInteger correct = new AtomicInteger();

        spectrumDOS.parallelStream().forEach(spectrumDO -> {
            List<SpectrumDO> librarySpectra = spectrumService.getByPrecursorMz(spectrumDO.getPrecursorMz(), mzTolerance, libraryId);
            List<SpectrumDO> decoyLibrarySpectra = spectrumService.getByPrecursorMz(spectrumDO.getPrecursorMz(), mzTolerance, decoyLibraryId);
            if (librarySpectra.size() != 0 || decoyLibrarySpectra.size() != 0) {
                correct.getAndIncrement();
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
                    incorrect.getAndIncrement();
                }
                LibraryHit libraryHit = new LibraryHit();
                libraryHit.setSpectrumId(librarySpectra.get(index).getId());
                libraryHit.setLibraryName(libraryId);
                libraryHit.setSmiles(librarySpectra.get(index).getSmiles());
                libraryHit.setScore(maxScore);
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
                decoyLibraryHit.setScore(maxScore);
                libraryHits.add(decoyLibraryHit);
            }
        });
        incorrect.set(incorrect.get() - correct.get());

        //找到满足FDR条件的分数阈值
        double threshold = 0.0;
        List<List<Double>> scores = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threshold = i * 0.01;
            double finalThreshold = threshold;
            List<LibraryHit> positiveHits = libraryHits.stream().filter(libraryHit -> libraryHit.getScore() > finalThreshold && !libraryHit.isDecoy()).toList();
            List<LibraryHit> negativeHits = libraryHits.stream().filter(libraryHit -> libraryHit.getScore() > finalThreshold && libraryHit.isDecoy()).toList();
            if (correct.get() + incorrect.get() == 0 || positiveHits.size() == 0) {
                continue;
            }
            double fdr = (double) negativeHits.size() / positiveHits.size() * incorrect.get() / (correct.get() + incorrect.get());
            List<Double> score = new ArrayList<>();
            score.add(threshold);
            score.add(positiveHits.size() + 0.0);
            score.add(negativeHits.size() + 0.0);
            score.add(fdr + 0.0);
            scores.add(score);
        }
        EasyExcel.write("/Users/anshaowei/Downloads/test_opt.xlsx").sheet("sheet1").doWrite(scores);

        log.info("finish, threshold: " + threshold);
    }

    @RequestMapping("report")
    public void report() {
        reporter.toMgf("test", "ST001794");
    }

}
