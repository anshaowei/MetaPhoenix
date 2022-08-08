package net.csibio.mslibrary.client.domain.db;

import lombok.Data;
import net.csibio.mslibrary.client.constants.AdductConst;
import net.csibio.mslibrary.client.domain.bean.Adduct;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashSet;

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

    /**
     * 靶标名称
     */
    @Indexed
    String name;

    /**
     * 靶标中文名
     */
    @Indexed
    String cnName;

    /**
     * 化学方程式
     */
    @Indexed
    String formula;

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
    String pubChemId;
    String casId;
    String drugBankId;
    String keggId;
    String hmdbId;
    String chemSpiderId;

    // 暂时无用的3个保留字段
    String superPathway;
    String subPathway;
    String pathwaySortOrder;

    // 创建日期
    Date createDate;
    // 最后修改日期
    Date lastModifiedDate;

    String features = "";

    String comments;

    /**
     * 将自己作为一个非主库的靶标进行克隆,如果本身就是一个主库靶标,那么会将自己的rootId设置为原靶标的id
     *
     * @return
     */
    public CompoundDO cloneAsNewComp() {
        CompoundDO clone = new CompoundDO();
        BeanUtils.copyProperties(this, clone);
        clone.setId(null);
        clone.setLibraryId(null);
        return clone;
    }

    public HashSet<Adduct> fetchAdductList() {
        this.adducts.addAll(AdductConst.adductProbabilitiesList);
        return this.adducts;
    }
}
