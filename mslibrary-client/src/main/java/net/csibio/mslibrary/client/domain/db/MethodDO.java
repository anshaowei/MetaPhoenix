package net.csibio.mslibrary.client.domain.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document("method")
public class MethodDO {

    @Id
    String id;

    @Indexed(unique = true)
    String name;

    //搜索谱图的mz精度
    Double mzTolerance;

    //控制mzTolerance的单位为ppm还是Da
    Boolean ppmForMzTolerance;

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

    Date createDate;

    Date lastModifiedDate;

    String description;

}
