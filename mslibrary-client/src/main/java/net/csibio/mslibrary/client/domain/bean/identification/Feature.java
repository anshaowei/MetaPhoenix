package net.csibio.mslibrary.client.domain.bean.identification;

import lombok.Data;
import net.csibio.mslibrary.client.domain.bean.adduct.Adduct;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;

import java.util.HashSet;
@Data
public class Feature {

    /**
     * 基础信息，生成FeatureDO时即会生成
     * 这里的值都是根据dataDO生成的平均值
     */
    Double mz;
    Double rt;
    Double area;
    String mainAdduct;
    SpectrumDO ms1Spectrum;
    SpectrumDO ms2Spectrum;
    HashSet<Adduct> adducts = new HashSet<>();

    /**
     * 鉴定信息，只有当进行了化合物库比对后才会填充
     */
    String compoundId;
    String compoundName;
    String formula;
    String metaPathways;
    Double matchScore;
    String smiles;
    String inChI;
    String structure2d;

}
