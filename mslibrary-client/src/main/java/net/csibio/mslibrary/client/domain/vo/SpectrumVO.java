package net.csibio.mslibrary.client.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.HashMap;

@Data
public class SpectrumVO {

    String projectName;
    String platform;

    Double rt;
    Double precursorMz;

    String id;

    String targetName;
    /**
     * 映射Compound库的id
     */
    String targetCode;

    /**
     * 谱图类型,可以是ms1,ms2
     */
    String type;

    double[] mzs;
    double[] ints;

    /**
     * 谱图来源,可以是标品Standard, Deconvolution, Raw等
     */
    String source;

    /**
     * 映射光谱当前所在库的id,不是建库的时候所使用的库id.
     * 当入主库的时候本id会变为主库的id,即-1
     * 如果要追溯建库时所使用的库id,请使用overviewId来溯源
     * 在本类中需要注意本字段与libraryName字段的区别
     */
    String libraryId;

    /**
     * 靶标cid
     */
    String innerId;
    /**
     * 光谱生成来源的基质
     */
    String matrix;

    /**
     * 光谱生成来源的物种
     */
    String species;

    // 谱图名称,自主命名,一般用于区分同一个靶标下的不同谱图,如果没有则按照instrument-energyLevel-voltage来命名
    String name;

    // 设备信息
    String instrument;

    /**
     * 碰撞能量等级,一般为low,med,high等等
     */
    String energyLevel;

    // 碰撞能量电压
    String voltage;

    /**
     * CID、HCD、ETD等
     * 1. CID:碰撞诱导裂解.(在低能CID(即在三重四极子或离子阱中碰撞诱导的离解)中,主要沿着其主干携带正电荷片段的肽段,主要产生b和y离子.
     * 对于包含RKNQ的片段,失去氨(-17 Da)的离子(a*,b*和y*)出现峰值,含STED的碎片,失水(-18 Da)记为a°,b°,y°.
     * 在高能碰撞光谱中可以观察到上述所有离子序列.相对丰度与组成有关.与低能量CID不同.离子不容易失去氨或水.
     * 2. ETD and ECD:电子转移离解和电子俘获离解.主要产生c、y、z+1和z+2离子.在某些情况下,w离子也会很明显.
     * 3. HCD:高能诱导裂解.改善CID裂解中产生的低质量碎片丢失效应.
     *
     */
    String fragMod;

    /**
     * ESI,EI等
     * ESI:电喷雾电离,软电离方法的一种.碎片离子峰很少,通常只有整体分子峰.适用于生物大分子.
     * EI:电子轰击电离,有较全的碎片离子峰信息,但是有时候分子离子峰强度很低甚至不出峰
     *
     */
    String ionizationMod;

    /**
     * 电离极性, Negative, Positive
     */
    String polarity;

    // 来自于的metapro的进样总览的id
    String overviewId;

    // 同一个来源下可能会采用多个Scan的质谱图,此时需要记录对应的scanId
    String scanId;

    // 使用标准品建库时所用的溶剂浓度
    String concentration;

    // 外部数据来源,目前可以是HMDB, KaiLaiPu等
    String outerSource;

    // 谱图的相关信息描述
    String description;

    // 可以冗余和overview相关的信息,比如libraryId, libraryName, batchNo等等
    HashMap<String, Object> featureMap = new HashMap<>();

    // 进样的创建日期
    Date createDate;

    // 最后修改日期
    Date lastModifiedDate;
}
