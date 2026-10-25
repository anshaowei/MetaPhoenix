package net.csibio.metaphoenix.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.metaphoenix.client.constants.LibraryConst;
import net.csibio.metaphoenix.client.constants.enums.ResultCode;
import net.csibio.metaphoenix.client.domain.Result;
import net.csibio.metaphoenix.client.domain.bean.parser.csv.CsvCompound;
import net.csibio.metaphoenix.client.domain.bean.parser.listener.ExcelListener;
import net.csibio.metaphoenix.client.domain.db.CompoundDO;
import net.csibio.metaphoenix.client.domain.db.LibraryDO;
import net.csibio.metaphoenix.client.service.CompoundService;
import net.csibio.metaphoenix.client.service.LibraryParserService;
import net.csibio.metaphoenix.client.service.LibraryService;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
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
            errorResult.setErrorResult(ResultCode.DUPLICATED_COMPOUND_EXIST);
            return errorResult;
        }
        try {
            compoundService.insert(compList, LibraryConst.Empty);
        } catch (Exception e) {
            compoundService.removeAllByLibraryId(library.getId());
            libraryService.remove(library.getId());
            return Result.Error(e.getMessage());
        }

        library.setCount(compList.size());
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

}
