package net.csibio.mslibrary.client.domain.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "gene")
@CompoundIndexes({@CompoundIndex(name = "libraryId_id", def = "{'libraryId':1,'id':1}", unique = true)})
public class GeneDO {

    /**
     * 基因标识符,第一位是来源数据库,第二位是Access号,第三位是名称
     * 如果需要通过UniProt进行查询,可以使用第二位Access进行查询
     * 例如: sp|B6J853|MIAB_COXB1
     */
    @Id
    String id;

    @Indexed
    String libraryId;

    @Indexed
    String identifyLine;

    /**
     * 蛋白质名称
     * 例如: tRNA-2-methylthio-N(6)-dimethylallyladenosine synthase
     */
    @Indexed
    List<String> names;

    @Indexed
    List<String> tags;

    @Indexed
    String organism;

    String sequence;

    Date createDate;

    Date lastModifiedDate;
}
