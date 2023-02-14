package net.csibio.mslibrary.client.domain.db;

import lombok.Data;
import net.csibio.mslibrary.client.constants.enums.DecoyProcedure;
import net.csibio.mslibrary.client.constants.enums.DecoyStrategy;
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

    Double mzTolerance;

    int ppm;

    //switch for ppm or Da
    Boolean ppmForMzTolerance;

    //取前几个最高分的结果
    Integer topN;

    Double threshold;

    //使用哪些谱图库进行检索
    List<String> libraryIds;

    /**
     * @see DecoyStrategy
     */
    String decoyStrategy;

    /**
     * @see DecoyProcedure
     */
    String decoyProcedure;

    /**
     * @see SpectrumMatchMethod
     */
    String spectrumMatchMethod;

    Date createDate;

    Date lastModifiedDate;

    String description;

}
