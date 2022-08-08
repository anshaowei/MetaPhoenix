package net.csibio.mslibrary.client.domain.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;

@Data
@Document(collection = "stat")
@CompoundIndexes({@CompoundIndex(name = "dim_type_date", def = "{'dim':1,'type':1,'date':1}", unique = true)})
public class StatDO {

    public static String DATE_FORMAT = "yyyyMMdd-HHmmss";
    public static String DATE_FORMAT_DAY = "yyyyMMdd";

    @Id
    String id;

    /**
     * 统计维度
     */
    @Indexed
    String dim;

    /**
     * 统计类型
     */
    @Indexed
    String type;

    @Indexed
    Date date;

    /**
     * Map格式的统计内容
     */
    HashMap<String, Object> statMap;

    /**
     * 统计日期
     */
    Date createDate;

    /**
     * 最后修改日期
     */
    Date lastModifiedDate;

    public StatDO(){}

    public StatDO(String dim, String type, Date date){
        this.dim = dim;
        this.type = type;
        this.date = date;
    }

}
