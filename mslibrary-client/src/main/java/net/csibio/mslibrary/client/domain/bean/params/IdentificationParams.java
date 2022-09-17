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

}
