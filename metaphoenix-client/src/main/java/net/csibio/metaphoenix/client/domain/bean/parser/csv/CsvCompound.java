package net.csibio.metaphoenix.client.domain.bean.parser.csv;

import com.alibaba.excel.annotation.ExcelProperty;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class CsvCompound {
    @ExcelProperty("cnName")
    @CsvBindByName
    String cnName;

    @ExcelProperty("name")
    @CsvBindByName(required = true)
    String name;
    @ExcelProperty("mz")
    @CsvBindByName(required = true)
    Double mz;
    @ExcelProperty("mass")
    @CsvBindByName
    Double mass;

    @ExcelProperty("ri")
    @CsvBindByName
    Double ri;

    @ExcelProperty("riTolerance")
    @CsvBindByName
    Double riTolerance;

    @ExcelProperty("rt")
    @CsvBindByName
    Double rt;

    @ExcelProperty("charge")
    @CsvBindByName
    Integer charge;

    @ExcelProperty("rtTolerance")
    @CsvBindByName
    Double rtTolerance;

    @ExcelProperty("type")
    @CsvBindByName
    String type;

    @ExcelProperty("formula")
    @CsvBindByName
    String formula;

    @ExcelProperty("superPathway")
    @CsvBindByName
    String superPathway;

    @ExcelProperty("subPathway")
    @CsvBindByName
    String subPathway;

    @CsvBindByName
    @ExcelProperty("chemSpiderId")
    String chemSpiderId;

    @CsvBindByName
    @ExcelProperty("pubchemId")
    String pubchemId;

    @CsvBindByName
    @ExcelProperty("casId")
    String casId;

    @CsvBindByName
    @ExcelProperty("keggId")
    String keggId;

    @CsvBindByName
    @ExcelProperty("hmdbId")
    String hmdbId;

    @CsvBindByName
    @ExcelProperty("mainAdduct")
    String mainAdduct;

    @CsvBindByName
    @ExcelProperty("ionForm")
    String ionForm;

    @CsvBindByName
    @ExcelProperty("structure2d")
    String structure2d;

    @CsvBindByName
    @ExcelProperty("delta")
    Double delta;

}
