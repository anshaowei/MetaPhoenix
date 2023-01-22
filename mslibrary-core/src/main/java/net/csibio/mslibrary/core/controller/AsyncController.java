package net.csibio.mslibrary.core.controller;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.LibraryConst;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.GeneDO;
import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.parser.fasta.FastaParser;
import net.csibio.mslibrary.client.parser.gnps.GnpsParser;
import net.csibio.mslibrary.client.parser.hmdb.HmdbParser;
import net.csibio.mslibrary.client.service.GeneService;
import net.csibio.mslibrary.client.service.LibraryParserService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.ProteinService;
import net.csibio.mslibrary.core.config.VMProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Autowired
    GnpsParser gnpsParser;

    @RequestMapping(value = "/hmdbGenes")
    Result hmdbGenes() throws XException {
        log.info("开始同步HMDB基因数据");
        String proteinPath = vmProperties.getRepository() + "/hmdb/gene.fasta";
        HashMap<String, String> geneMap = fastaParser.parse(proteinPath);
        String libraryId = LibraryConst.HMDB_GENE;
        List<GeneDO> genes = geneService.buildGenes(geneMap);
        geneService.storeToDB(genes, libraryId);
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
        proteinService.storeToDB(proteins, libraryId);
        log.info("HMDB蛋白质数据同步完成");
        return Result.OK();
    }

    @RequestMapping(value = "/uniprotProteins")
    Result uniprotProteins() throws XException {
        log.info("开始同步Uniprot蛋白质数据");
        String humanFasta = vmProperties.getRepository() + "/uniprot/reviewed_human.fasta";
        String mouseFasta = vmProperties.getRepository() + "/uniprot/reviewed_mouse.fasta";
        String ratFasta = vmProperties.getRepository() + "/uniprot/reviewed_rat.fasta";
        String riceFasta = vmProperties.getRepository() + "/uniprot/reviewed_rice.fasta";
        String libraryId = LibraryConst.UNIPROT_PROTEIN;
        HashMap<String, String> humanReviewed = fastaParser.parse(humanFasta);
        HashMap<String, String> mouseReviewed = fastaParser.parse(mouseFasta);
        HashMap<String, String> ratReviewed = fastaParser.parse(ratFasta);
        HashMap<String, String> riceReviewed = fastaParser.parse(riceFasta);

        List<ProteinDO> human = proteinService.buildProteins(humanReviewed, "human");
        List<ProteinDO> mouse = proteinService.buildProteins(mouseReviewed, "mouse");
        List<ProteinDO> rat = proteinService.buildProteins(ratReviewed, "rat");
        List<ProteinDO> rice = proteinService.buildProteins(riceReviewed, "rice");
        human.addAll(mouse);
        human.addAll(rat);
        human.addAll(rice);
        proteinService.storeToDB(human, libraryId);
        log.info("Uniprot蛋白质数据同步完成");
        return Result.OK();
    }

    @RequestMapping(value = "/panhuman2020")
    Result localPeptides() throws XException {
        log.info("开始同步PanHuman2020-DPHL蛋白-肽段DIA数据");
        String panHuman = vmProperties.getRepository() + "/dia/DPHL1_v1.csv";
        String libraryId = LibraryConst.PAN_HUMAN_2020_DPHL;

        log.info("PanHuman2020-DPHL蛋白-肽段DIA数据同步完成");
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
