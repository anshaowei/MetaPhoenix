package net.csibio.mslibrary.client.domain.vo;

import lombok.Data;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

@Data
public class LibraryVO {

    private static final long serialVersionUID = -3259329283915356327L;

    String id;

    String name;

    String platform;

    /**
     * @see net.csibio.metapro.client.constants.enums.LibraryType
     */
    String type;

    /**
     * 靶标建库来自的基质,可以是standard,plasma,urine等
     */
    List<String> matrix;

    /**
     * 物种,例如HUMAN,也可以是多种物种的组合
     */
    List<String> species;

    // 库中含有的靶标的数量
    Integer targetCount = 0;

    // 库中所有的靶标对应的光谱库光谱数目
    Long spectraCount = 0L;

    String description;

    // 创建日期
    Date createDate;

    // 最后修改日期
    Date lastModifiedDate;

    public LibraryVO() {
    }

    public LibraryVO(LibraryDO library) {
        BeanUtils.copyProperties(library, this);
    }
}
