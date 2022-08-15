package net.csibio.mslibrary.core.controller;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.LibraryConst;
import net.csibio.mslibrary.client.constants.enums.LibraryType;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.GeneDO;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.domain.query.CompoundQuery;
import net.csibio.mslibrary.client.domain.query.GeneQuery;
import net.csibio.mslibrary.client.domain.query.ProteinQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.*;
import net.csibio.mslibrary.core.config.VMProperties;
import net.csibio.mslibrary.core.parser.fasta.FastaParser;
import net.csibio.mslibrary.core.parser.hmdb.HmdbParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

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
    @Autowired
    ProteinService proteinService;
    @Autowired
    GeneService geneService;
    @Autowired
    LibraryService libraryService;

    @RequestMapping(value = "/hmdbGenes")
    Result hmdbGenes() throws XException {
        log.info("开始同步HMDB基因数据");
        String proteinPath = vmProperties.getRepository() + "/hmdb/gene.fasta";
        HashMap<String, String> geneMap = fastaParser.parse(proteinPath);

        List<GeneDO> genes = geneService.buildGenes(geneMap);
        LibraryDO library = libraryService.getById(LibraryConst.HMDB_PROTEIN);
        if (library == null) {
            library = new LibraryDO();
            library.setType(LibraryType.Geneics.getName());
            library.setName(LibraryConst.HMDB_GENE);
            libraryService.insert(library);
            log.info("HMDB蛋白质镜像库不存在,已创建新的HMDB蛋白质库");
        }
        genes.forEach(gene -> {
            fastaParser.hmdbFormat(gene);
            gene.setLibraryId(LibraryConst.HMDB_GENE);
        });
        geneService.remove(new GeneQuery(LibraryConst.HMDB_GENE));

        geneService.insert(genes);
        library.setCount(genes.size());
        libraryService.update(library);
        log.info("HMDB蛋白质数据同步完成");
        return Result.OK();
    }

    @RequestMapping(value = "/hmdbCompounds")
    Result hmdbCompounds() {
        log.info("开始同步HMDB化合物数据");
        String path = vmProperties.getRepository() + "/hmdb/metabolites.xml";
        hmdbParser.parse(path);
        log.info("HMDB数据同步完成");
        return Result.OK();
    }

    @RequestMapping(value = "/hmdbProteins")
    Result hmdbProteins() throws XException {
        log.info("开始同步HMDB蛋白质数据");
        String proteinPath = vmProperties.getRepository() + "/hmdb/protein.fasta";
        HashMap<String, String> proteinMap = fastaParser.parse(proteinPath);

        List<ProteinDO> proteins = proteinService.buildProteins(proteinMap);
        LibraryDO library = libraryService.getById(LibraryConst.HMDB_PROTEIN);
        if (library == null) {
            library = new LibraryDO();
            library.setType(LibraryType.Proteomics.getName());
            library.setName(LibraryConst.HMDB_PROTEIN);
            libraryService.insert(library);
            log.info("HMDB蛋白质镜像库不存在,已创建新的HMDB蛋白质库");
        }
        proteins.forEach(protein -> {
            fastaParser.hmdbFormat(protein);
            protein.setLibraryId(LibraryConst.HMDB_PROTEIN);
        });
        proteinService.remove(new ProteinQuery(LibraryConst.HMDB_PROTEIN));

        proteinService.insert(proteins);
        library.setCount(proteins.size());
        libraryService.update(library);
        log.info("HMDB蛋白质数据同步完成");
        return Result.OK();
    }

    @RequestMapping(value = "/uniprotProteins")
    Result uniprotProteins() throws XException {
        log.info("开始同步Fasta数据");
        String humanFasta = vmProperties.getRepository() + "/uniprot/reviewed_human.fasta";

        HashMap<String, String> humanReviewed = fastaParser.parse(humanFasta);
        List<ProteinDO> proteins = proteinService.buildProteins(humanReviewed, "human");
        proteins.forEach(protein -> fastaParser.uniprotFormat(protein));

        LibraryDO library = libraryService.getById(LibraryConst.HMDB_PROTEIN);
        if (library == null) {
            library = new LibraryDO();
            library.setName(LibraryConst.HMDB_PROTEIN);
            libraryService.insert(library);
            log.info("HMDB蛋白质镜像库不存在,已创建新的HMDB蛋白质库");
        }
        proteinService.remove(new ProteinQuery(LibraryConst.HMDB_PROTEIN));

        log.info("Fasta数据同步完成");
        return Result.OK();
    }

}
