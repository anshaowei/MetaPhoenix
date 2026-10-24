package net.csibio.mslibrary.core.controller;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.parser.gnps.GnpsParser;
import net.csibio.mslibrary.client.parser.hmdb.HmdbParser;
import net.csibio.mslibrary.client.service.LibraryParserService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.core.config.VMProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("async")
public class AsyncController {

    @Autowired
    LibraryParserService libraryParserService;
    @Autowired
    VMProperties vmProperties;
    @Autowired
    HmdbParser hmdbParser;
    @Autowired
    LibraryService libraryService;
    @Autowired
    GnpsParser gnpsParser;

    @RequestMapping(value = "/hmdbCompounds")
    Result hmdbCompounds() {
        log.info("开始同步HMDB化合物数据");
        String path = vmProperties.getRepository() + "/hmdb/metabolites.xml";
        hmdbParser.parse(path);
        log.info("HMDB数据同步完成");
        return Result.OK();
    }

    @RequestMapping(value = "/gnps")
    Result gnpsMetabolites() throws XException {
        log.info("开始同步GNPS化合物数据");
        String gnps = vmProperties.getRepository() + "/ALL_GNPS.json";
        gnpsParser.parseJSON(gnps);
        return Result.OK();
    }
}
