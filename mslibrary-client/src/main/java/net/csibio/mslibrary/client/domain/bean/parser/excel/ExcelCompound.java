package net.csibio.mslibrary.client.domain.bean.parser.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ExcelCompound {

    @ExcelProperty("cnName")
    String cnName;

    @ExcelProperty("name")
    String name;

    @ExcelProperty("mz")
    Double mz;

    @ExcelProperty("mass")
    Double mass;

    @ExcelProperty("ri")
    Double ri;

    @ExcelProperty("riTolerance")
    Double riTolerance;

    @ExcelProperty("rt")
    Double rt;

    @ExcelProperty("charge")
    Integer charge;

    @ExcelProperty("rtTolerance")
    Double rtTolerance;

    @ExcelProperty("type")
    String type;

    @ExcelProperty("forRi")
    Boolean forRi;

    @ExcelProperty("formula")
    String formula;

    @ExcelProperty("superPathway")
    String superPathway;

    @ExcelProperty("subPathway")
    String subPathway;

    @ExcelProperty("species")
    String species;

    @ExcelProperty("matrix")
    String matrix;

    @ExcelProperty("platform")
    String platform;

    @ExcelProperty("chemSpiderId")
    String chemSpiderId;

    @ExcelProperty("pubchemId")
    String pubchemId;

    @ExcelProperty("casId")
    String casId;

    @ExcelProperty("keggId")
    String keggId;

    @ExcelProperty("hmdbId")
    String hmdbId;

    @ExcelProperty("mainAdduct")
    String mainAdduct;

    @ExcelProperty("ionForm")
    String ionForm;

    @ExcelProperty("structure2d")
    String structure2d;

    @ExcelProperty("structure2d")
    String oldId;

    @ExcelProperty("order")
    String order;

    @ExcelProperty("delta")
    Double delta;

}
