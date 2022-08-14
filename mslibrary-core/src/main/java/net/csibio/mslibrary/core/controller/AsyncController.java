package net.csibio.mslibrary.core.controller;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.service.*;
import net.csibio.mslibrary.core.config.VMProperties;
import net.csibio.mslibrary.core.parser.fasta.FastaParser;
import net.csibio.mslibrary.core.parser.hmdb.HmdbParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

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
    FastaParser fastaParser;

    @RequestMapping(value = "/hmdb")
    Result hmdb() {
        log.info("开始同步HMDB数据");
        String path = vmProperties.getRepository()+"/hmdb/hmdb_metabolites.xml";
        hmdbParser.parse(path);
        log.info("HMDB数据同步完成");
        return Result.OK();
    }

    @RequestMapping(value = "/fasta")
    Result fasta() throws FileNotFoundException {
        log.info("开始同步Fasta数据");
        String humanFasta = vmProperties.getRepository()+"/fasta/reviewed_human.fasta";
        FileInputStream fis = new FileInputStream(humanFasta);
        Result<HashMap<String, String>> res = fastaParser.parse(fis);

        log.info("Fasta数据同步完成");
        return Result.OK();
    }

    @RequestMapping(value = "/async")
    Result async() {
        String path = vmProperties.getRepository()+"/hmdb_metabolites.xml";
        hmdbParser.parse(path);
        return Result.OK();
    }

}
