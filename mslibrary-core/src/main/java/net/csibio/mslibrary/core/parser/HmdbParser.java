package net.csibio.mslibrary.core.parser;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.LibraryConst;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.hmdb.Descendant;
import net.csibio.mslibrary.client.domain.bean.hmdb.Property;
import net.csibio.mslibrary.client.domain.bean.hmdb.Taxonomy;
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
                    Iterator iter = maim.elementIterator();
                    while (iter.hasNext()) {
                        Element ele = (Element) iter.next();
                        if (ele.getName().equals("metabolite")) {
                            break;
                        }

                        String value = ele.getStringValue();
                        if (value.isEmpty()){
                            break;
                        }
                        switch (ele.getName()){
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
                            case "secondary_accessions" ->  compound.setHmdbIds(parseList(ele));
                            case "synonyms" ->  compound.setSynonyms(parseList(ele));
                            case "taxonomy" ->  compound.setTaxonomy(parseTaxonomy(ele));
                            case "ontology" ->  compound.setOntology(parseDescendantList(ele));
                            case "experimental_properties" ->  compound.setExperimentalProperties(parseProperties(ele));
                            case "predicted_properties" ->  compound.setPredictedProperties(parseProperties(ele));
                        }
                    }
                    compounds.add(compound);
                }
            }
            log.info("总计扫描到化合物"+compounds.size()+"个,正在删除旧数据");
            compoundService.remove(new CompoundQuery(LibraryConst.HMDB), LibraryConst.HMDB);
            log.info("旧数据删除完毕,开始插入新数据");
            long insertTime = System.currentTimeMillis();
            compoundService.insert(compounds, LibraryConst.HMDB);
            log.info("新数据插入完毕,数据库插入耗时:"+(System.currentTimeMillis() - insertTime)/1000+"秒;总耗时:"+(System.currentTimeMillis() - start)/1000+"秒");
        } catch (DocumentException | ParseException e) {
            e.printStackTrace();
        }
        return new Result(true);
    }

    private Taxonomy parseTaxonomy(Element taxonomyElement){
        Iterator iter = taxonomyElement.elementIterator();
        Taxonomy taxonomy = new Taxonomy();
        while (iter.hasNext()) {
            Element ele = (Element) iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()){
                break;
            }
            switch (ele.getName()){
                case "description" -> taxonomy.setDescription(value);
                case "direct_parent" -> taxonomy.setDirectParent(value);
                case "kingdom" -> taxonomy.setKingdom(value);
                case "super_class" -> taxonomy.setSuperClass(value);
                case "class" -> taxonomy.setClazz(value);
                case "sub_class" -> taxonomy.setSubClass(value);
                case "molecular_framework" -> taxonomy.setMolecularFramework(value);
                case "alternative_parents" -> taxonomy.setAlterParents(parseList(taxonomyElement));
                case "substituents" -> taxonomy.setSubstituents(parseList(taxonomyElement));
                case "external_descriptors" -> taxonomy.setExtDesc(parseList(taxonomyElement));
            }
        }
       return taxonomy;
    }

    private List<Descendant> parseDescendantList(Element ontology){
        Iterator iter = ontology.elementIterator();
        List<Descendant> descendants = new ArrayList<>();
        while (iter.hasNext()) {
            Element ele = (Element) iter.next();
            Descendant descendant = parseDescendant(ele);
            descendants.add(descendant);
        }
        return descendants;
    }

    private Descendant parseDescendant(Element element){
        Iterator iter = element.elementIterator();
        Descendant descendant = new Descendant();
        while (iter.hasNext()) {
            Element ele = (Element) iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()){
                break;
            }
            switch (ele.getName()){
                case "term" -> descendant.setTerm(value);
                case "definition" -> descendant.setDefinition(value);
                case "parent_id" -> descendant.setParentId(value);
                case "level" -> descendant.setLevel(Integer.parseInt(value));
                case "type" -> descendant.setType(value);
                case "synonyms" -> descendant.setSynonyms(parseList(ele));
                case "descendants" -> descendant.setDescendants(parseDescendantList(ele));
            }
        }
        return descendant;
    }

    private List<Property> parseProperties(Element element){
        Iterator iter = element.elementIterator();
        List<Property> properties = new ArrayList<>();
        while (iter.hasNext()) {
            Element ele = (Element) iter.next();
            Property property = parseProperty(ele);
            properties.add(property);
        }
        return properties;
    }

    private Property parseProperty(Element element){
        Iterator iter = element.elementIterator();
        Property property = new Property();
        while (iter.hasNext()) {
            Element ele = (Element) iter.next();
            String value = ele.getStringValue();
            if (value.isEmpty()){
                break;
            }
            switch (ele.getName()){
                case "kind" -> property.setKind(value);
                case "value" -> property.setValue(value);
                case "source" -> property.setSource(value);
            }
        }
        return property;
    }

    private List<String> parseList(Element node){
        Iterator iter = node.elementIterator();
        List<String> list = new ArrayList<>();
        while (iter.hasNext()) {
            Element ele = (Element) iter.next();
            String value = ele.getStringValue();
            list.add(value);
        }
        return list;
    }
}
