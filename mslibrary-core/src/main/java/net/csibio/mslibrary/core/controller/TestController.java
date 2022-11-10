package net.csibio.mslibrary.core.controller;


import io.github.msdk.MSDKException;
import net.csibio.mslibrary.client.algorithm.search.CommonSearch;
import net.csibio.mslibrary.client.domain.bean.params.IdentificationParams;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.parser.gnps.CompoundGenerator;
import net.csibio.mslibrary.client.parser.gnps.GnpsParser;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("test")
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

    @RequestMapping("/1")
    public void test1() {
        gnpsParser.parse("/Users/anshaowei/Downloads/database/ALL_GNPS.json");
    }

    @RequestMapping("/2")
    public void test2() {
//        compoundService.removeAll();
        spectrumService.removeAll();
    }

    @RequestMapping("/3")
    public void test3() throws MSDKException {
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

}
