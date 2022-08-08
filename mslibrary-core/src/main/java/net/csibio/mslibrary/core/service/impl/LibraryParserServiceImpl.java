package net.csibio.mslibrary.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.parser.csv.CsvCompound;
import net.csibio.mslibrary.client.domain.bean.parser.listener.ExcelListener;
import net.csibio.mslibrary.client.domain.bean.spectrum.AnnotationHistory;
import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryParserService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.opencsv.ICSVWriter.NO_QUOTE_CHARACTER;

@Slf4j
@Service("libraryParserService")
public class LibraryParserServiceImpl implements LibraryParserService {

    @Autowired
    CompoundService compoundService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;

    public Result parse(InputStream in, LibraryDO library, int fileFormat) {
        List<CsvCompound> csvTargetList = null;
        if (fileFormat == 1) {
            //excel的读取方法
            ExcelReaderBuilder read = EasyExcel.read(in, CsvCompound.class, new ExcelListener());
            csvTargetList = read.doReadAllSync();
        } else {
            InputStreamReader reader = new InputStreamReader(in);
            HeaderColumnNameMappingStrategy<CsvCompound> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(CsvCompound.class);
            CsvToBean<CsvCompound> csvToBean = new CsvToBeanBuilder<CsvCompound>(reader).withQuoteChar(NO_QUOTE_CHARACTER).withSeparator('\t').withEscapeChar('\\').withMappingStrategy(strategy).build();
            try {
                csvTargetList = csvToBean.parse();
            } catch (Exception e) {
                libraryService.remove(library.getId());
                if (e.getCause() instanceof CsvRequiredFieldEmptyException) {
                    CsvRequiredFieldEmptyException ex = (CsvRequiredFieldEmptyException) e.getCause();
                    return Result.Error("错误行:" + ex.getLineNumber() + ";" + ex.getMessage());
                } else {
                    return Result.Error(e.getMessage());
                }
            }

        }

        List<CompoundDO> compList = new ArrayList<>();
        csvTargetList.forEach(csvTarget -> {
            CompoundDO compound = new CompoundDO();
            BeanUtils.copyProperties(csvTarget, compound);
            compound.setLibraryId(library.getId());
            compList.add(compound);
        });

        HashSet<CompoundDO> compSet = new HashSet<CompoundDO>();
        List<String> errorNames = new ArrayList<>();
        compList.forEach(target -> {
            if (compSet.contains(target)) {
                errorNames.add(target.getName());
            } else {
                compSet.add(target);
            }
        });
        if (errorNames.size() > 0) {
            libraryService.remove(library.getId());
            Result errorResult = new Result(false);
            errorResult.setErrorList(errorNames);
            errorResult.setErrorResult(ResultCode.DUPLICATED_TARGET_EXIST);
            return errorResult;
        }
        try {
            compoundService.insert(compList);
        } catch (Exception e) {
            compoundService.removeAllByLibraryId(library.getId());
            libraryService.remove(library.getId());
            return Result.Error(e.getMessage());
        }

        library.setTargetCount(compList.size());
        libraryService.update(library);
        log.info("库" + library.getName() + "创建成功!," + compList.size() + "条靶标插入成功");
        Result result = new Result(true);
        result.setData(library);
        return result;
    }

    /**
     * @param line
     * @Description: 对每一行的数据清洗
     * @return: java.util.HashMap<java.lang.String, java.lang.Integer>
     **/
    private HashMap<String, Integer> parseColumns(String line) {
        String[] columns = line.split(SymbolConst.TAB);
        HashMap<String, Integer> columnMap = new HashMap<>();
        for (int i = 0; i < columns.length; i++) {
            columnMap.put(StringUtils.deleteWhitespace(columns[i].toLowerCase()), i);
        }
        return columnMap;
    }

    /**
     * 实现基于XML格式的HMDB化合物库信息解析
     *
     * @param filePath
     * @return
     */
    @Override
    public Result parseHMDB(String filePath) {

        try {
            //获取sax解析器的工厂对象
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            //通过工厂对象创建解析器对象
            SAXParser saxParser = parserFactory.newSAXParser();
            //编写处理器

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        SAXReader reader = new SAXReader();
        List<CompoundDO> compoundDOS = new ArrayList<>();

        try {
            Document document = reader.read(filePath);
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            //以metabolite标签为一个迭代来读取文件信息
            while (iterator.hasNext()) {
                Element element = (Element) iterator.next();
                //每次遇到metabolite就创建一个新化合物
                if (element.getName().equals("metabolite")) {
                    CompoundDO compoundDO = new CompoundDO();
                    //此迭代用以遍历两个metabolite标签之间的内容
                    Iterator iterator1 = element.elementIterator();
                    while (iterator1.hasNext()) {
                        Element element1 = (Element) iterator1.next();
                        if ((element1).getName().equals("metabolite")) {
                            break;
                        }
                        if (element1.getName().equals("chemical_formula")) {
                            compoundDO.setFormula(element1.getStringValue());
                        }
                        if (element1.getName().equals("name")) {
                            compoundDO.setName(element1.getStringValue());
                        }
                        if (element1.getName().equals("creation_date")) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                            compoundDO.setCreateDate(simpleDateFormat.parse(element1.getStringValue()));
                        }
                        if (element1.getName().equals("update_date")) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                            compoundDO.setLastModifiedDate(simpleDateFormat.parse(element1.getStringValue()));
                        }
                        if (element1.getName().equals("average_molecular_weight")) {
                            if (!element1.getStringValue().isEmpty()) {
                                compoundDO.setAvgMw(Double.parseDouble(element1.getStringValue()));
                            }
                        }
                        if (element1.getName().equals("monisotopic_molecular_weight")) {
                            if (!element1.getStringValue().isEmpty()) {
                                compoundDO.setMonoMw(Double.parseDouble(element1.getStringValue()));
                            }
                        }
                        if (element1.getName().equals("smiles")) {
                            compoundDO.setSmiles(element1.getStringValue());
                        }
                    }
                    compoundDOS.add(compoundDO);
                }
            }
        } catch (DocumentException | ParseException e) {
            e.printStackTrace();
        }
        return new Result(true);
    }


    /**
     * 输入MassBank格式的JSON文件，能够解析外部谱图并生成对应化合物
     *
     * @param filePath
     * @return
     */
    @Override
    public Result parseMassBank(String filePath) {

        File file = new File(filePath);

        //解析谱图及化合物
        HashMap<String, List<SpectrumDO>> compoundNameToSpectrumMap = new HashMap<>();
        HashMap<String, CompoundDO> compoundNameToCompoundMap = new HashMap<>();
        try {
            JSONArray jsonArray = JSONArray.parseArray(FileUtils.readFileToString(file, "UTF-8"));
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = JSONObject.parseObject(jsonArray.getString(i));
                SpectrumDO spectrumDO = new SpectrumDO();
                //解析谱图字段信息
                spectrumDO.setSpectrumId(jsonObject.get("spectrum_id").toString());
                spectrumDO.setSourceFile(jsonObject.get("source_file").toString());
                spectrumDO.setTask(jsonObject.get("task").toString());
                spectrumDO.setScan(Integer.valueOf(jsonObject.get("scan").toString()));
                spectrumDO.setMsLevel(Integer.valueOf(jsonObject.get("ms_level").toString()));
                spectrumDO.setLibraryMembership(jsonObject.get("library_membership").toString());
                spectrumDO.setSpectrumStatus(Integer.valueOf(jsonObject.get("spectrum_status").toString()));
                spectrumDO.setSplash(jsonObject.get("splash").toString());
                spectrumDO.setSubmitUser(jsonObject.get("submit_user").toString());
                spectrumDO.setCompoundName(jsonObject.get("Compound_Name").toString());
                spectrumDO.setIonSource(jsonObject.get("Ion_Source").toString());
                spectrumDO.setCompoundSource(jsonObject.get("Compound_Source").toString());
                spectrumDO.setInstrument(jsonObject.get("Instrument").toString());
                spectrumDO.setPi(jsonObject.get("PI").toString());
                spectrumDO.setDataCollector(jsonObject.get("Data_Collector").toString());
                spectrumDO.setAdduct(jsonObject.get("Adduct").toString());
                spectrumDO.setPrecursorMz(Double.parseDouble(jsonObject.get("Precursor_MZ").toString()));
                spectrumDO.setExactMass(Double.parseDouble(jsonObject.get("ExactMass").toString()));
                spectrumDO.setCharge(Integer.valueOf(jsonObject.get("Charge").toString()));
                spectrumDO.setCasNumber(jsonObject.get("CAS_Number").toString());
                spectrumDO.setPubmedId(jsonObject.get("Pubmed_ID").toString());
                spectrumDO.setSmiles(jsonObject.get("Smiles").toString());
                spectrumDO.setInchi(jsonObject.get("INCHI").toString());
                spectrumDO.setInchiAUX(jsonObject.get("INCHI_AUX").toString());
                spectrumDO.setLibraryClass(Integer.valueOf(jsonObject.get("Library_Class").toString()));
                spectrumDO.setIonMode(jsonObject.get("Ion_Mode").toString());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                spectrumDO.setCreateTime(simpleDateFormat.parse(jsonObject.get("create_time").toString()));
                spectrumDO.setTaskId(jsonObject.get("task_id").toString());
                spectrumDO.setUserId(jsonObject.get("user_id").toString());
                spectrumDO.setInchiKeySmiles(jsonObject.get("InChIKey_smiles").toString());
                spectrumDO.setInchiKeyInchi(jsonObject.get("InChIKey_inchi").toString());
                spectrumDO.setFormulaSmiles(jsonObject.get("Formula_smiles").toString());
                spectrumDO.setFormulaInchi(jsonObject.get("Formula_inchi").toString());
                spectrumDO.setUrl(jsonObject.get("url").toString());

                //解析谱图数据信息
                String[] values = jsonObject.get("peaks_json").toString().replace("[", "").replace("]", "").replace(",", " ").split(" ");
                int length = values.length / 2;
                double[] mzs = new double[length];
                double[] intensities = new double[length];
                for (int k = 0; k < length; k++) {
                    mzs[k] = Double.parseDouble(values[2 * k]);
                    intensities[k] = Double.parseDouble(values[2 * k + 1]);
                }
                spectrumDO.setMzs(mzs);
                spectrumDO.setInts(intensities);

                //解析化合物，按照化合物名称进行唯一识别
                String compoundName = jsonObject.get("Compound_Name").toString();
                if (compoundNameToCompoundMap.containsKey(compoundName)) {
                    compoundNameToSpectrumMap.get(compoundName).add(spectrumDO);
                } else {
                    CompoundDO compoundDO = new CompoundDO();
                    //设定化合物信息
                    compoundDO.setName(compoundName);
                    compoundDO.setPubChemId(spectrumDO.getPubmedId());
                    compoundDO.setSmiles(spectrumDO.getSmiles());
                    compoundNameToCompoundMap.put(compoundName, compoundDO);
                    List<SpectrumDO> spectrumDOS = new ArrayList<>();
                    spectrumDOS.add(spectrumDO);
                    compoundNameToSpectrumMap.put(compoundName, spectrumDOS);
                }
            }
            //插入数据库
            int spectraCount = 0;
            for (String compoundName : compoundNameToSpectrumMap.keySet()) {
                CompoundDO compoundDO = compoundNameToCompoundMap.get(compoundName);
                compoundService.insert(compoundDO);
                List<SpectrumDO> spectrumDOS = compoundNameToSpectrumMap.get(compoundName);
                for (SpectrumDO spectrumDO : spectrumDOS) {
                    spectrumDO.setCompoundId(compoundDO.getId());
                    spectrumService.insert(spectrumDO);
                    spectraCount++;
                }
            }
            log.info("解析MassBank谱图完成，共插入" + compoundNameToCompoundMap.keySet().size() + "个化合物，" + spectraCount + "张谱图");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(true);
    }


    /**
     * 输入GNPS格式的JSON文件，能够实现外部谱图的导入及化合物的生成
     *
     * @param filePath
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @Override
    public Result parseGNPS(String filePath) throws IOException, ParseException {

        long startTime = System.currentTimeMillis();
        JsonFactory jsonFactory = new MappingJsonFactory();
        JsonParser parser = jsonFactory.createParser(new File(filePath));
        List<SpectrumDO> spectrumDOS = new ArrayList<>();
        int spectrumCount = 0;
        log.info("开始执行GNPS数据库解析任务");
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                if (parser.getCurrentName().equals("spectrum_id")) {
                    spectrumCount++;
                }
            }
        }
        log.info("初步扫描完成，文件共包含" + spectrumCount + "张谱图");

        parser = jsonFactory.createParser(new File(filePath));
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                if (parser.getCurrentName().equals("spectrum_id")) {
                    SpectrumDO spectrumDO = new SpectrumDO();
                    parser.nextToken();
                    spectrumDO.setSpectrumId(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setSourceFile(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setTask(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setScan(parser.getValueAsInt());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setMsLevel(parser.getValueAsInt());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setLibraryMembership(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setSpectrumStatus(parser.getValueAsInt());
                    parser.nextToken();
                    parser.nextToken();
                    String[] values = parser.getValueAsString().replace("[", "").replace("]", "").replace(",", " ").split(" ");
                    ;
                    int length = values.length / 2;
                    double[] mzs = new double[length];
                    double[] intensities = new double[length];
                    for (int k = 0; k < length; k++) {
                        mzs[k] = Double.parseDouble(values[2 * k]);
                        intensities[k] = Double.parseDouble(values[2 * k + 1]);
                    }
                    spectrumDO.setMzs(mzs);
                    spectrumDO.setInts(intensities);
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setSplash(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setSubmitUser(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setCompoundName(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setIonSource(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setCompoundSource(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setInstrument(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setPi(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setDataCollector(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setAdduct(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setPrecursorMz(parser.getValueAsDouble());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setExactMass(parser.getValueAsDouble());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setCharge(parser.getValueAsInt());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setCasNumber(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setPubmedId(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setSmiles(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setInchi(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setInchiAUX(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setLibraryClass(parser.getValueAsInt());
                    parser.nextToken();
                    parser.nextToken();
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setIonMode(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    spectrumDO.setCreateTime(simpleDateFormat.parse(parser.getValueAsString()));
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setTaskId(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setUserId(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setInchiKeySmiles(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setInchiKeyInchi(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setFormulaSmiles(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setFormulaInchi(parser.getValueAsString());
                    parser.nextToken();
                    parser.nextToken();
                    spectrumDO.setUrl(parser.getValueAsString());
                    List<AnnotationHistory> annotationHistoryList = new ArrayList<>();
                    jsonToken = parser.currentToken();
                    while (!parser.isClosed()) {
                        jsonToken = parser.nextToken();
                        if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                            if (parser.getCurrentName().equals("Compound_Name")) {
                                AnnotationHistory annotationHistory = new AnnotationHistory();
                                parser.nextToken();
                                annotationHistory.setCompoundName(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setIonSource(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setCompoundSource(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setInstrument(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setPi(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setDataCollector(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setAdduct(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setScan(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setPrecursorMz(parser.getValueAsDouble());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setExactMass(parser.getValueAsDouble());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setCharge(parser.getValueAsInt());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setCasNumber(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setPubmedId(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setSmiles(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setInchi(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setInchiAUX(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setLibraryClass(parser.getValueAsInt());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setSpectrumId(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setIonMode(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setCreateTime(simpleDateFormat.parse(parser.getValueAsString()));
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setTaskId(parser.getValueAsString());
                                parser.nextToken();
                                parser.nextToken();
                                annotationHistory.setUserId(parser.getValueAsString());

                                annotationHistoryList.add(annotationHistory);
                            }
                        }
                        if (JsonToken.END_ARRAY.equals(jsonToken)) {
                            break;
                        }
                    }
                    spectrumDO.setAnnotationHistoryList(annotationHistoryList);
                    spectrumDOS.add(spectrumDO);
                }
            }
        }
        log.info("解析完成，共用时" + (System.currentTimeMillis() - startTime) / 1000 + "秒，开始向数据库插入");
        spectrumService.insert(spectrumDOS);
        log.info("向数据库共插入" + spectrumCount + "张谱图完成，共用时" + (System.currentTimeMillis() - startTime) / 1000 + "秒");
        return new Result(true);
    }
}
