package net.csibio.mslibrary.client.domain.db;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import net.csibio.aird.compressor.bytecomp.ZlibWrapper;
import net.csibio.mslibrary.client.domain.bean.Adduct;
import net.csibio.mslibrary.client.domain.bean.hmdb.*;
import net.csibio.mslibrary.client.utils.CompressUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Data
@Document(collection = "compound")
@CompoundIndexes({
        @CompoundIndex(name = "libraryId_code", def = "{'libraryId':1,'code':1}", unique = true),
        @CompoundIndex(name = "libraryId_name", def = "{'libraryId':1,'name':1}", unique = true)})
public class CompoundDO {

    @Id
    String id;

    @Indexed
    String libraryId;

    /**
     * 每一个靶标对应一个识别码,和基质,采集模式等无关,可以手动设定
     */
    @Indexed
    String code;

    @Indexed
    String status;

    @Indexed
    String name;

    /**
     * 靶标中文名
     */
    @Indexed
    String cnName;

    /**
     * 化合物名称的同义词
     */
    List<String> synonyms;

    /**
     * 化学方程式
     */
    @Indexed
    String formula;

    /**
     * 有机物命名法
     */
    String iupac;

    /**
     * 传统有机物命名法
     */
    String traditionalIupac;

    /**
     * 国际化合物标识 International Chemical Identifier
     */
    String inchi;

    /**
     * 国际标识物key
     */
    String inchikey;

    /**
     * 形态
     */
    String state;

    /**
     * 平均分子质量
     */
    Double avgMw;

    /**
     * 天然同位素加权平均分子质量
     */
    Double monoMw;

    /**
     * 当前主要的加和物形式
     */
    String mainAdduct;

    /**
     * 关联的加和物列表
     */
    HashSet<Adduct> adducts = new HashSet<>();

    /**
     * 分子结构式二维图的网络url地址
     */
    String structure2d;

    /**
     * Smiles图
     */
    String smiles;

    /**
     * 综合参考
     */
    String synthesisReference;

    /**
     * 在外部数据库的ID
     */
    String pubChemId;
    String casId;
    String drugBankId;
    String foodbId;
    String keggId;
    String hmdbId;  //即Accession
    List<String> hmdbIds;
    String chemSpiderId;
    String pdbId;
    String chebiId;
    String phenolExplorerId;
    String knapsackId;
    String biocycId;
    String biggId;
    String wikipediaId;
    String metlinId;
    String vmhId;
    String fbontoId;

    // 创建日期
    Date createDate;
    // 最后修改日期
    Date lastModifiedDate;

    String description;

    byte[] zipHmdbInfo;

    @Transient
    HmdbInfo hmdbInfo;

    public void encode() {
        zipHmdbInfo = new ZlibWrapper().encode(JSON.toJSONBytes(hmdbInfo));
    }

    public void decode() {
        if (zipHmdbInfo != null) {
            byte[] unzip = new ZlibWrapper().decode(zipHmdbInfo);
            hmdbInfo = JSON.parseObject(unzip, HmdbInfo.class);
        }
    }
}
