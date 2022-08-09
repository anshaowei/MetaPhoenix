package net.csibio.mslibrary.core.parser;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.LibraryConst;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.query.CompoundQuery;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class HmdbParser implements IParser {

    @Autowired
    CompoundService compoundService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;

    @Override
    public Result parse(String filePath) {
        LibraryDO library = libraryService.getById(LibraryConst.HMDB);
        if (library == null) {
            library = new LibraryDO();
            library.setId(LibraryConst.HMDB);
            library.setName(LibraryConst.HMDB);
            libraryService.insert(library);
            log.info("HMDB镜像库不存在,已创建新的HMDB库");
        }
        try {
            //获取sax解析器的工厂对象
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            //通过工厂对象创建解析器对象
            SAXParser saxParser = parserFactory.newSAXParser();
            //编写处理器

        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        SAXReader reader = new SAXReader();
        List<CompoundDO> compounds = new ArrayList<>();
        log.info("开始解析源文件");
        try {
            long start = System.currentTimeMillis();
            Document document = reader.read(filePath);
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            //以metabolite标签为一个迭代来读取文件信息
            while (iterator.hasNext()) {
                Element maim = (Element) iterator.next();
                //每次遇到metabolite就创建一个新化合物
                if (maim.getName().equals("metabolite")) {
                    CompoundDO compound = new CompoundDO();
                    compound.setLibraryId(library.getId());
                    //此迭代用以遍历两个metabolite标签之间的内容
                    Iterator iterator1 = maim.elementIterator();
                    while (iterator1.hasNext()) {
                        Element property = (Element) iterator1.next();
                        if (property.getName().equals("metabolite")) {
                            break;
                        }

                        String value = property.getStringValue();
                        if (value.isEmpty()){
                            break;
                        }
                        switch (property.getName()){
                            case "name" -> compound.setName(value);
                            case "accession" -> compound.setHmdbId(value);
                            case "status" -> compound.setStatus(value);
                            case "chemical_formula" -> compound.setFormula(value);
                            case "average_molecular_weight" -> compound.setAvgMw(Double.parseDouble(value));
                            case "monisotopic_molecular_weight" -> compound.setMonoMw(Double.parseDouble(value));
                            case "smiles" -> compound.setSmiles(value);
                            case "description" -> compound.setDescription(value);
                            case "iupac_name" -> compound.setIupac(value);
                            case "traditional_iupac" -> compound.setTraditionalIupac(value);
                            case "cas_registry_number" -> compound.setCasId(value);
                            case "chemspider_id" -> compound.setChemSpiderId(value);
                            case "drugbank_id" -> compound.setDrugBankId(value);
                            case "pubchem_compound_id" -> compound.setPubChemId(value);
                            case "phenol_explorer_compound_id" -> compound.setPhenolExplorerId(value);
                            case "knapsack_id" -> compound.setKnapsackId(value);
                            case "kegg_id" -> compound.setKeggId(value);
                            case "foodb_id" -> compound.setFoodbId(value);
                            case "biocyc_id" -> compound.setBiocycId(value);
                            case "bigg_id" -> compound.setBiggId(value);
                            case "wikipedia_id" -> compound.setWikipediaId(value);
                            case "metlin_id" -> compound.setMetlinId(value);
                            case "vmh_id" -> compound.setVmhId(value);
                            case "fbonto_id" -> compound.setFbontoId(value);
                            case "synthesis_reference" -> compound.setSynthesisReference(value);
                            case "inchi" -> compound.setInchi(value);
                            case "inchikey" -> compound.setInchikey(value);
                            case "state" -> compound.setState(value);
                            case "creation_date" ->  compound.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(value));
                            case "update_date" ->  compound.setLastModifiedDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(value));
                        }
                    }
                    compounds.add(compound);
                }
            }
            log.info("总计扫描到化合物"+compounds.size()+"个,正在删除旧数据");
            compoundService.remove(new CompoundQuery(), LibraryConst.HMDB);
            log.info("旧数据删除完毕,开始插入新数据");
            long insertTime = System.currentTimeMillis();
            compoundService.insert(compounds, LibraryConst.HMDB);
            log.info("新数据插入完毕,数据库插入耗时:"+(System.currentTimeMillis() - insertTime)/1000+"秒;总耗时:"+(System.currentTimeMillis() - start)/1000+"秒");
        } catch (DocumentException | ParseException e) {
            e.printStackTrace();
        }
        return new Result(true);
    }
}
