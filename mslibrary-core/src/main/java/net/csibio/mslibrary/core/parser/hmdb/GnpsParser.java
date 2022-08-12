package net.csibio.mslibrary.core.parser.hmdb;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.AdductConst;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.Adduct;
import net.csibio.mslibrary.client.domain.bean.spectrum.AnnotationHistory;
import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.checkerframework.checker.units.qual.A;
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

    public Result parse(String filePath) {

        long startTime = System.currentTimeMillis();
        JsonFactory jsonFactory = new MappingJsonFactory();
        JsonParser parser = null;
        try {
            parser = jsonFactory.createParser(new File(filePath));
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
            List<String> compoundNames = new ArrayList<>();
            HashMap<String, String> libraryNameToIdMap = new HashMap<>();
            HashMap<String, List<String>> libraryIdToCompoundNamesMap = new HashMap<>();
            HashMap<String, String> compoundNameToIdMap = new HashMap<>();
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

                        //化合物库生成
                        String libraryId;
                        if (libraryNameToIdMap.containsKey(spectrumDO.getLibraryMembership())) {
                            LibraryDO libraryDO = libraryService.getById(libraryNameToIdMap.get(spectrumDO.getLibraryMembership()));
                            libraryId = libraryDO.getId();
                        } else {
                            LibraryDO libraryDO = new LibraryDO();
                            libraryDO.setName(spectrumDO.getLibraryMembership());
                            libraryService.insert(libraryDO);
                            libraryNameToIdMap.put(libraryDO.getName(), libraryDO.getId());
                            libraryId = libraryDO.getId();
                            libraryIdToCompoundNamesMap.put(libraryId, new ArrayList<>());
                        }

                        //化合物生成
                        if (!libraryIdToCompoundNamesMap.get(libraryId).contains(spectrumDO.getCompoundName())) {
                            CompoundDO compoundDO = new CompoundDO();
                            compoundDO.setName(spectrumDO.getCompoundName());
                            //加和物判断
                            for (Adduct adduct : AdductConst.ESIAdducts) {
                                if (adduct.getIonForm().equals(spectrumDO.getAdduct())) {
                                    HashSet<Adduct> adducts = new HashSet<>();
                                    adducts.add(adduct);
                                    compoundDO.setAdducts(adducts);
                                }
                            }
                            compoundDO.setFormula(spectrumDO.getFormulaSmiles());
                            compoundDO.setInchi(spectrumDO.getInchi());
                            compoundDO.setInchikey(spectrumDO.getInchiKeyInchi());
                            compoundDO.setSmiles(spectrumDO.getSmiles());
                            compoundDO.setPubChemId(spectrumDO.getPubmedId());
                            libraryIdToCompoundNamesMap.get(libraryId).add(compoundDO.getName());
                            compoundService.insert(compoundDO, libraryId);
                            compoundNameToIdMap.put(compoundDO.getName(), compoundDO.getId() + "&" + libraryId);
                        } else {
                            CompoundDO compoundDO = compoundService.getById(compoundNameToIdMap.get(spectrumDO.getCompoundName()).split("&")[0], libraryId);
                            if (!spectrumDO.getAdduct().isEmpty()) {
                                Adduct currentAdduct = new Adduct();
                                for (Adduct adduct : AdductConst.ESIAdducts) {
                                    if (adduct.getIonForm().equals(spectrumDO.getAdduct())) {
                                        currentAdduct = adduct;
                                    }
                                }
                                if (!compoundDO.getAdducts().contains(currentAdduct)) {
                                    compoundDO.getAdducts().add(currentAdduct);
                                }
                                compoundService.update(compoundDO, libraryId);
                            }
                        }
                    }
                }
            }
            log.info("解析完成，共用时" + (System.currentTimeMillis() - startTime) / 1000 + "秒，开始向数据库插入");
            spectrumService.insert(spectrumDOS);
            log.info("向数据库共插入" + spectrumCount + "张谱图完成，共用时" + (System.currentTimeMillis() - startTime) / 1000 + "秒");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(true);
    }

}
