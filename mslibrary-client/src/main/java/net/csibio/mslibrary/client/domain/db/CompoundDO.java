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

    @Indexed
    String cnName;

    List<String> synonyms;

    @Indexed
    String formula;

    String iupac;

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
    @Indexed
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
     * 在外部数据库的ID
     */
    @Indexed
    String pubChemId;
    @Indexed
    String casId;
    @Indexed
    String drugBankId;
    @Indexed
    String foodbId;
    @Indexed
    String keggId;
    @Indexed
    String hmdbId;  //即Accession

    List<String> hmdbIds;
    @Indexed
    String chemSpiderId;
    @Indexed
    String pdbId;
    @Indexed
    String chebiId;
    @Indexed
    String phenolExplorerId;
    @Indexed
    String knapsackId;
    @Indexed
    String biocycId;
    @Indexed
    String biggId;
    @Indexed
    String wikipediaId;
    @Indexed
    String metlinId;
    @Indexed
    String vmhId;
    @Indexed
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
