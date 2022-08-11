package net.csibio.mslibrary.core.controller;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.service.*;
import net.csibio.mslibrary.core.config.VMProperties;
import net.csibio.mslibrary.core.parser.hmdb.HmdbParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("admin")
public class AdminController {

    @Autowired
    LibraryParserService libraryParserService;
    @Autowired
    VMProperties vmProperties;
    @Autowired
    HmdbParser hmdbParser;

    @RequestMapping(value = "/asyncHMDB")
    Result asyncHMDB() {
        String path = vmProperties.getRepository()+"/hmdb_metabolites.xml";
//        String path = vmProperties.getRepository()+"/hmdbSingle.xml";
        hmdbParser.parse(path);
        return Result.OK();
    }

    @RequestMapping(value = "/async")
    Result async() {
        String path = vmProperties.getRepository()+"/hmdb_metabolites.xml";
//        String path = vmProperties.getRepository()+"/hmdbSingle.xml";
        hmdbParser.parse(path);
        return Result.OK();
    }

}
