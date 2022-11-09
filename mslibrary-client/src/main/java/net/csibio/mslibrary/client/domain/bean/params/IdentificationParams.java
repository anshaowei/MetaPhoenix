package net.csibio.mslibrary.client.domain.bean.params;

import lombok.Data;

import java.util.List;

@Data
public class IdentificationParams {

    //搜索谱图的mz精度
    Double mzTolerance;

    //取前几个最高分的结果
    Integer topN;

    //使用哪些谱图库进行检索
    List<String> libraryIds;

    /**
     * 谱图数据库匹配策略
     * 策略1：只匹配满足给定仪器、激活器、能量、谱图类型、极性的谱图
     * 策略2：主要参考匹配分数，对其他附加信息不设限
     */
    Integer strategy;

}
