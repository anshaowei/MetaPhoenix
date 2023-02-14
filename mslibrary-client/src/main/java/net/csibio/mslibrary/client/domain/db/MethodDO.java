package net.csibio.mslibrary.client.domain.db;

import lombok.Data;
import net.csibio.mslibrary.client.constants.enums.DecoyProcedure;
import net.csibio.mslibrary.client.constants.enums.SpectrumMatchMethod;
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

    Double threshold;

    //使用哪些谱图库进行检索
    List<String> libraryIds;

    /**
     * @see DecoyProcedure
     */
    String strategy;

    /**
     * @see SpectrumMatchMethod
     */
    String similarityType;

    Date createDate;

    Date lastModifiedDate;

    String description;

}
