package net.csibio.mslibrary.core.controller;


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
import net.csibio.mslibrary.client.parser.gnps.CompoundGenerator;
import net.csibio.mslibrary.client.parser.gnps.GnpsParser;
import net.csibio.mslibrary.client.parser.gnps.MspGNPSParser;
import net.csibio.mslibrary.client.parser.hmdb.SpectrumParser;
import net.csibio.mslibrary.client.parser.massbank.MspMassBankParser;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    CompoundGenerator compoundGenerator;

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

    @RequestMapping("/importLibrary")
    public void importLibrary() {
//        gnpsParser.parseJSON("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.json");
//        mspMassBankParser.parse("/Users/anshaowei/Documents/Metabolomics/library/MassBank/MassBank_NIST.msp");
        mspGNPSParser.parse("/Users/anshaowei/Documents/Metabolomics/library/GNPS/ALL_GNPS.msp");
    }

    @RequestMapping("/clean")
    public void clean() {
        //数据库清理
        LibraryDO libraryDO = libraryService.getById("GNPS");
        //1. 只有存在smiles和precursorMz的谱图会被保留
        List<SpectrumDO> spectrumDOS = spectrumService.getAll(new SpectrumQuery(), libraryDO.getId());
        int count = spectrumDOS.size();
        //remove spectrum without smiles
        spectrumDOS.removeIf(spectrumDO -> spectrumDO.getSmiles() == null || spectrumDO.getSmiles().equals("") || spectrumDO.getSmiles().equals("N/A") || spectrumDO.getSmiles().equals("NA")
                || spectrumDO.getPrecursorMz() == null || spectrumDO.getPrecursorMz() == 0);
        spectrumService.remove(new SpectrumQuery(), libraryDO.getId());
        log.info("remove " + (count - spectrumDOS.size()) + " spectra without smiles or have null precursorMz");
        spectrumService.insert(spectrumDOS, libraryDO.getId());

        //查看谱图根据smiles分类后的分布情况
//        HashMap<String, List<SpectrumDO>> smilesMap = new HashMap<>();
//        for (SpectrumDO spectrumDO : spectrumDOS) {
//            if (smilesMap.containsKey(spectrumDO.getSmiles())) {
//                smilesMap.get(spectrumDO.getSmiles()).add(spectrumDO);
//            } else {
//                List<SpectrumDO> list = new ArrayList<>();
//                list.add(spectrumDO);
//                smilesMap.put(spectrumDO.getSmiles(), list);
//            }
//        }
//        int maxSmiles = Integer.MIN_VALUE;
//        int minSmiles = Integer.MAX_VALUE;
//        int average = 0;
//        for (String smiles : smilesMap.keySet()) {
//            List<SpectrumDO> list = smilesMap.get(smiles);
//            average += list.size();
//            if (list.size() > maxSmiles) {
//                maxSmiles = list.size();
//            }
//            if (list.size() < minSmiles) {
//                minSmiles = list.size();
//            }
//        }
//        average = average / smilesMap.keySet().size();
//        log.info("maxSmiles: " + maxSmiles);
//        log.info("minSmiles: " + minSmiles);
//        log.info("average: " + average);

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
        SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
        IAtomContainer m = sp.parseSmiles("c1ccccc1");
        for (IAtom atom : m.atoms()) {
            atom.setImplicitHydrogenCount(null);
            int a = 0;
        }

    }

    @RequestMapping("decoy")
    public void decoy() {
        spectrumGenerator.spectrumBasedGenerate("MassBank");
    }

}
