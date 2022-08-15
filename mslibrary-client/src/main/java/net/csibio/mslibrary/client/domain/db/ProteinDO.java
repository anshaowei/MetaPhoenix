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
@Document(collection = "protein")
public class ProteinDO {

    /**
     * 蛋白质标识符,第一位是来源数据库,第二位是Access号,第三位是名称
     * 如果需要通过UniProt进行查询,可以使用第二位Access进行查询
     * 例如: sp|B6J853|MIAB_COXB1
     */
    @Id
    String id;

    @Indexed
    String libraryId;

    @Indexed
    String identifyLine;

    @Indexed
    String uniprot;

    @Indexed
    Boolean reviewed;

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

    @Indexed
    String gene;

    String sequence;

    Date createDate;

    Date lastModifiedDate;

    public String getUniProtLink() {
        if (id != null) {
            String[] identifiers = id.split("\\|", -1);
            if (identifiers.length == 3) {
                return "https://www.uniprot.org/uniprot/" + identifiers[1];
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public String getAlphaFoldLink() {
        if (id != null) {
            String[] identifiers = id.split("\\|", -1);
            if (identifiers.length == 3) {
                return "https://www.alphafold.ebi.ac.uk/entry/" + identifiers[1];
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
