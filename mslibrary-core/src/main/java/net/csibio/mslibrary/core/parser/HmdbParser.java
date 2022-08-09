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
                Element element = (Element) iterator.next();
                //每次遇到metabolite就创建一个新化合物
                if (element.getName().equals("metabolite")) {
                    CompoundDO compound = new CompoundDO();
                    compound.setLibraryId(library.getId());
                    //此迭代用以遍历两个metabolite标签之间的内容
                    Iterator iterator1 = element.elementIterator();
                    while (iterator1.hasNext()) {
                        Element element1 = (Element) iterator1.next();
                        if (element1.getName().equals("metabolite")) {
                            break;
                        }
                        if (element1.getName().equals("chemical_formula")) {
                            compound.setFormula(element1.getStringValue());
                        }
                        if (element1.getName().equals("name")) {
                            compound.setName(element1.getStringValue());
                        }
                        if (element1.getName().equals("creation_date")) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                            compound.setCreateDate(simpleDateFormat.parse(element1.getStringValue()));
                        }
                        if (element1.getName().equals("update_date")) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                            compound.setLastModifiedDate(simpleDateFormat.parse(element1.getStringValue()));
                        }
                        if (element1.getName().equals("average_molecular_weight")) {
                            if (!element1.getStringValue().isEmpty()) {
                                compound.setAvgMw(Double.parseDouble(element1.getStringValue()));
                            }
                        }
                        if (element1.getName().equals("monisotopic_molecular_weight")) {
                            if (!element1.getStringValue().isEmpty()) {
                                compound.setMonoMw(Double.parseDouble(element1.getStringValue()));
                            }
                        }
                        if (element1.getName().equals("smiles")) {
                            compound.setSmiles(element1.getStringValue());
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
