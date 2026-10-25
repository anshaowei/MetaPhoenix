package net.csibio.metaphoenix.client.domain.db;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import net.csibio.aird.compressor.bytecomp.ZlibWrapper;
import net.csibio.metaphoenix.client.domain.bean.adduct.Adduct;
import net.csibio.metaphoenix.client.domain.bean.hmdb.Biological;
import net.csibio.metaphoenix.client.domain.bean.hmdb.HmdbInfo;
import net.csibio.metaphoenix.client.domain.bean.hmdb.Pathway;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Data
@Document(collection = "compound")
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

    /**
     * 数据库中化合物谱图的数量
     */
    Integer count = 0;

    @Indexed
    String name;

    String version;

    @Indexed
    String cnName;

    List<String> synonyms;

    @Indexed
    String formula;

    @Indexed
    String iupac;

    @Indexed
    String traditionalIupac;

    /**
     * 国际化合物标识 International Chemical Identifier
     */
    @Indexed
    String inchi;

    /**
     * 国际标识物key
     */
    @Indexed
    String inchikey;

    /**
     * 形态
     */
    @Indexed
    String state;

    /**
     * 平均分子质量
     */
    @Indexed
    Double avgMw;

    /**
     * 天然同位素加权平均分子质量
     */
    @Indexed
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
     * Smiles图
     */
    @Indexed
    String smiles;

    @Indexed
    List<String> cellulars;
    @Indexed
    List<String> bioSpecimens;
    @Indexed
    List<String> tissues;

    List<Pathway> pathways;

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

    @Indexed
    Date createDate;
    @Indexed
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

    public void setBiological(Biological biological) {
        setBioSpecimens(biological.getBioSpecimens());
        setCellulars(biological.getCellulars());
        setTissues(biological.getTissues());
        setPathways(biological.getPathways());
    }

    public Double calculateMz(Adduct adduct) {
        return (this.monoMw + adduct.getMw()) / adduct.getCharge();
    }

}
