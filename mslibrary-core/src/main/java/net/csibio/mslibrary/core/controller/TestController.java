package net.csibio.mslibrary.core.controller;


import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.parser.gnps.CompoundGenerator;
import net.csibio.mslibrary.client.parser.gnps.GnpsParser;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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

    @RequestMapping("/1")
    public void test1() {
//        gnpsParser.parse("/Users/anshaowei/Downloads/database/ALL_GNPS.json");


        LibraryQuery query = new LibraryQuery();
        List<LibraryDO> libraryDOList = libraryService.getAll(query);
        List<String> libraryIds = libraryDOList.stream().map(LibraryDO::getId).toList();
        compoundGenerator.generateByInChI(libraryIds);
    }

    @RequestMapping("/removeAllCompounds")
    public void test2() {
        compoundService.removeAll();
    }
}
