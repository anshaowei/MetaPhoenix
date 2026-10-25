package net.csibio.metaphoenix.client.domain.db;

import lombok.Data;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.metaphoenix.client.constants.enums.LibraryType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Document("library")
public class LibraryDO implements Serializable {

    private static final long serialVersionUID = -3259329283915356627L;

    @Id
    String id;

    /**
     * 库名称
     */
    @Indexed(unique = true)
    String name;

    /**
     * @see LibraryType
     */
    @Indexed
    String type;

    /**
     * 靶标建库来自的基质,可以是standard,plasma,urine等,也可以是多种基质的组合 @see Matrix
     */
    @Indexed
    Set<String> matrix = new HashSet<>();

    /**
     * 物种,例如HUMAN,也可以是多种物种的组合
     */
    @Indexed
    Set<String> species = new HashSet<>();

    Set<String> tags = new HashSet<>();

    Set<String> labels = new HashSet<>();

    /**
     * 库中条目的总数目
     */
    Integer count = 0;

    /**
     * 库中含有的谱图的数量
     */
    Integer spectrumCount = 0;

    /**
     * 库中含有的靶标的数量
     */
    String description;

    Date createDate;

    Date lastModifiedDate;

    // 新增一个基质类型
    public void addMatrix(String matrix) {
        this.matrix.add(matrix.toLowerCase());
    }

    // 新增一个物种类型
    public void addSpecies(String species) {
        this.species.add(species.toLowerCase());
    }

    public void setMatrixStr(String matrix) {
        this.setMatrix(matrix != null ? new HashSet<>(Arrays.asList(matrix.split(SymbolConst.COMMA))) : null);
    }

    public void setSpeciesStr(String species) {
        this.setSpecies(species != null ? new HashSet<>(Arrays.asList(species.split(SymbolConst.COMMA))) : null);
    }
}
