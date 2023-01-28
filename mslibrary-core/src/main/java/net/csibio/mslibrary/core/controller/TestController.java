package net.csibio.mslibrary.core.controller;


import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.algorithm.compound.Generator;
import net.csibio.mslibrary.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.mslibrary.client.algorithm.search.CommonSearch;
import net.csibio.mslibrary.client.algorithm.similarity.Similarity;
import net.csibio.mslibrary.client.constants.Constants;
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
import net.csibio.mslibrary.client.utils.ArrayUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.exception.CDKException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    MongoTemplate mongoTemplate;
    @Autowired
    Generator generator;

    @RequestMapping("/importLibrary")
    public void importLibrary() {
//        gnpsParser.parseJSON("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.json");
        mspMassBankParser.parse("/Users/anshaowei/Documents/Metabolomics/library/MassBank/MassBank_NIST.msp");
        mspGNPSParser.parse("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.msp");
    }

    @RequestMapping("/clean")
    public void clean() {
        String libraryId = "MassBank";
        List<SpectrumDO> spectrumDOS = spectrumService.getAll(new SpectrumQuery(), libraryId);
        int count = spectrumDOS.size();
        spectrumDOS.removeIf(spectrumDO -> spectrumDO.getSmiles() == null || spectrumDO.getSmiles().equals("") || spectrumDO.getSmiles().equals("N/A") || spectrumDO.getSmiles().equals("NA")
                || spectrumDO.getPrecursorMz() == null || spectrumDO.getPrecursorMz() == 0 || spectrumDO.getMzs() == null || spectrumDO.getInts() == null || spectrumDO.getMzs().length == 1 || spectrumDO.getInts().length == 1);
        spectrumDOS.removeIf(spectrumDO -> ArrayUtil.findNearestDiff(spectrumDO.getMzs(), spectrumDO.getPrecursorMz()) > spectrumDO.getPrecursorMz() * 10 * Constants.PPM);
        spectrumService.remove(new SpectrumQuery(), libraryId);
        spectrumService.insert(spectrumDOS, libraryId);
        log.info("remove " + (count - spectrumDOS.size()) + " spectra");
    }

    @RequestMapping("/clear")
    public void clear() {
        //delete all the database
        List<LibraryDO> libraryDOS = libraryService.getAll(new LibraryQuery());
        for (LibraryDO libraryDO : libraryDOS) {
            spectrumService.remove(new SpectrumQuery(), libraryDO.getId());
            mongoTemplate.dropCollection("spectrum-" + libraryDO.getId());
        }
        libraryService.removeAll();
    }

    @RequestMapping("/remove")
    public void remove() {
//        compoundService.removeByLibraryId("GNPS");
//        spectrumService.removeByLibraryId("GNPS");
//        libraryService.removeByLibraryId("GNPS");
    }

    @RequestMapping("/identify")
    public void identify() {
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

    @RequestMapping("inchi")
    public void inchi() throws CDKException {
//        SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
//        IAtomContainer m = sp.parseSmiles("c1ccccc1");
//        for (IAtom atom : m.atoms()) {
//            atom.setImplicitHydrogenCount(null);
//            int a = 0;
//        }

    }

    @RequestMapping("decoy")
    public void decoy() {
        spectrumGenerator.spectrumBased("MassBank");
    }

    @RequestMapping("generate")
    public void generate() {
        generator.generateBySmiles("MassBank");
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
        FileInputStream in = new FileInputStream(new File("/Users/anshaowei/Downloads/ST001794/ST001794.xlsx"));
        Workbook workbook = null;
        workbook = new XSSFWorkbook(in);
        Sheet sheet = workbook.getSheetAt(4);
        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            for (int index = 0; index < row.getPhysicalNumberOfCells(); index++) {
                Cell cell = row.getCell(index);

            }
        }
    }

}
