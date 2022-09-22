package net.csibio.mslibrary.client.domain.bean.identification;

import lombok.Data;
import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.domain.bean.adduct.Adduct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Data
public class Feature {

    /**
     * 基础信息，生成FeatureDO时即会生成
     * 这里的值都是根据dataDO生成的平均值
     */
    Double mz;

    Double rt;

    String ionForm;

    Spectrum ms1Spectrum;

    Spectrum ms2Spectrum;

    HashSet<Adduct> adducts = new HashSet<>();

    /**
     * 鉴定信息，只有当进行了化合物库比对后才会填充
     */
    List<IdentificationInfo> identificationInfos = new ArrayList<>();

}
