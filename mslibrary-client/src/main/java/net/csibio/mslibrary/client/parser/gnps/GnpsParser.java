package net.csibio.mslibrary.client.parser.gnps;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.enums.LibraryType;
import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.spectrum.AnnotationHistory;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Component
public class GnpsParser {

    @Autowired
    CompoundService compoundService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;

    /**
     * 该功能解析并将谱图插入数据库
     *
     * @param filePath
     * @return
     */
    public Result parse(String filePath) {

        long startTime = System.currentTimeMillis();
        JsonFactory jsonFactory = new MappingJsonFactory();
        JsonParser parser;

        try {
            parser = jsonFactory.createParser(new File(filePath));
            int spectrumCount = 0;

            //文件估算及预解析
            HashSet<String> libraryNames = new HashSet<>();
            HashSet<String> compoundIndexes = new HashSet<>();

            log.info("开始执行GNPS数据库解析任务");
            while (!parser.isClosed()) {
                JsonToken jsonToken = parser.nextToken();
                if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                    if (parser.getCurrentName().equals("spectrum_id")) {
                        spectrumCount++;
                    }
                    if (parser.getCurrentName().equals("library_membership")) {
                        parser.nextToken();
                        libraryNames.add(parser.getValueAsString());
                        String libraryName = parser.getValueAsString();
                        int i = 0;
                        while (i <= 9) {
                            parser.nextToken();
                            i++;
                        }
                        compoundIndexes.add(libraryName + "@" + parser.getValueAsString());
                    }
                }
            }
            log.info("初步扫描完成，文件共包含" + libraryNames.size() + "个库，大约" + compoundIndexes.size() + "个化合物，" + spectrumCount + "张谱图");

            //生成化合物库
            List<LibraryDO> libraryDOS = new ArrayList<>();
            for (String libraryName : libraryNames) {
                LibraryDO libraryDO = new LibraryDO();
                libraryDO.setName(libraryName);
                libraryDO.setType(LibraryType.Metabolomics.getName());
                libraryDOS.add(libraryDO);
            }

            //插入数据库
            if (libraryService.insert(libraryDOS).isFailed()) {
                log.error("生成化合物库失败，数据库中已存在同名数据库");
                Result result = new Result(false);
                return result.setErrorResult(ResultCode.LIBRARY_WITH_SAME_NAME_ALREADY_EXIST);
            }
            log.info("化合物库插入数据库成功");

            HashMap<String, List<SpectrumDO>> spectrumMap = new HashMap<>();
            for (String libraryName : libraryNames) {
                spectrumMap.put(libraryName, new ArrayList<>());
            }

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
                        spectrumDO.setLibraryId(parser.getValueAsString());
                        parser.nextToken();
                        parser.nextToken();
                        spectrumDO.setSpectrumStatus(parser.getValueAsInt());
                        parser.nextToken();
                        parser.nextToken();
                        String[] values = parser.getValueAsString().replace("[", "").replace("]", "").replace(" ", "").replace(",", " ").split(" ");
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
                        spectrumDO.setInchI(parser.getValueAsString());
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
                        //jsonToken = parser.currentToken();
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
                        spectrumMap.get(spectrumDO.getLibraryMembership()).add(spectrumDO);
                    }
                }
            }
            log.info("解析完成，共用时" + (System.currentTimeMillis() - startTime) / 1000 + "秒，开始向数据库插入");
            for (String libraryName : spectrumMap.keySet()) {
                LibraryDO libraryDO = libraryService.getById(libraryName);
                libraryDO.setSpectrumCount(spectrumMap.get(libraryName).size());
                libraryService.update(libraryDO);
                spectrumService.insert(spectrumMap.get(libraryName), libraryDO.getId());
            }
            log.info("向数据库共插入" + spectrumCount + "张谱图完成，共用时" + (System.currentTimeMillis() - startTime) / 1000 + "秒");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(true);
    }

}
