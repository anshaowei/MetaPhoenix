package net.csibio.mslibrary.core.controller;


import io.github.msdk.MSDKException;
import io.github.msdk.io.mgf.MgfFileImportMethod;
import io.github.msdk.io.mgf.MgfMsSpectrum;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.parser.gnps.CompoundGenerator;
import net.csibio.mslibrary.client.parser.gnps.GnpsParser;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
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
        //文件解析
        MgfFileImportMethod mgfFileImportMethod = new MgfFileImportMethod(new File("/Users/anshaowei/Downloads/(Centroid)_Met_08_Sirius.mgf"));
        List<MgfMsSpectrum> mgfMsSpectrums = mgfFileImportMethod.execute();

        int a = 0;
    }

}
