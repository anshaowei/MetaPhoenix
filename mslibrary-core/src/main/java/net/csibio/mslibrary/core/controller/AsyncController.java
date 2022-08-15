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
        String libraryId = LibraryConst.HMDB_GENE;
        List<GeneDO> genes = geneService.buildGenes(geneMap);
        LibraryDO library = libraryService.getById(libraryId);
        if (library == null) {
            library = new LibraryDO();
            library.setType(LibraryType.Genomics.getName());
            library.setName(libraryId);
            libraryService.insert(library);
            log.info("HMDB蛋白质镜像库不存在,已创建新的HMDB蛋白质库");
        }
        genes.forEach(gene -> {
            fastaParser.hmdbFormat(gene);
            gene.setLibraryId(libraryId);
        });
        geneService.remove(new GeneQuery(), libraryId);

        geneService.insert(genes, libraryId);
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
        String libraryId = LibraryConst.HMDB_PROTEIN;
        List<ProteinDO> proteins = proteinService.buildProteins(proteinMap);
        LibraryDO library = libraryService.getById(libraryId);
        if (library == null) {
            library = new LibraryDO();
            library.setType(LibraryType.Proteomics.getName());
            library.setName(libraryId);
            libraryService.insert(library);
            log.info("HMDB蛋白质镜像库不存在,已创建新的HMDB蛋白质库");
        }
        proteins.forEach(protein -> {
            fastaParser.hmdbFormat(protein);
            protein.setLibraryId(libraryId);
        });
        proteinService.remove(new ProteinQuery(), libraryId);

        proteinService.insert(proteins, libraryId);
        library.setCount(proteins.size());
        libraryService.update(library);
        log.info("HMDB蛋白质数据同步完成");
        return Result.OK();
    }

    @RequestMapping(value = "/uniprotProteins")
    Result uniprotProteins() throws XException {
        log.info("开始同步Uniprot蛋白质数据");
        String humanFasta = vmProperties.getRepository() + "/uniprot/reviewed_human.fasta";

        String libraryId = LibraryConst.UNIPROT_PROTEIN;
        HashMap<String, String> humanReviewed = fastaParser.parse(humanFasta);
        List<ProteinDO> proteins = proteinService.buildProteins(humanReviewed, "human");

        LibraryDO library = libraryService.getById(libraryId);
        if (library == null) {
            library = new LibraryDO();
            library.setName(libraryId);
            library.setType(LibraryType.Proteomics.getName());
            libraryService.insert(library);
            log.info("Uniprot蛋白质镜像库不存在,已创建新的Uniprot蛋白质库");
        }
        proteinService.remove(new ProteinQuery(), libraryId);
        proteins.forEach(protein -> {
            fastaParser.uniprotFormat(protein);
            protein.setLibraryId(libraryId);
        });

        proteinService.insert(proteins, libraryId);
        library.setCount(proteins.size());
        libraryService.update(library);
        log.info("Uniprot蛋白质数据同步完成");
        return Result.OK();
    }

}
