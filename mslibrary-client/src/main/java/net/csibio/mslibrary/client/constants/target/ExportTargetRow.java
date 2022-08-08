package net.csibio.mslibrary.client.constants.target;

import lombok.Data;

import java.util.HashMap;

@Data
public class ExportTargetRow {

    String targetId;

    // 靶标名称
    String name;

    // 靶标的质荷比,带电模式下
    Double mz;

    // 标准化的时间描述
    Double ri;

    // 化学方程式
    String formula;

    String matrix;

    String platform;

    String species;

    //单电荷时的计算方式为mz-mass的值
    Double delta;
    // 带电量
    Integer charge;

    // 当前靶标在所在基质所在采集模式下的离子形式
    String ionForm;

    // 当前主要的加和物形式
    String mainAdduct;

    // 预测出峰时间, 单位分钟
    Double rt;

    // rt窗口, 只考虑 ± rtWindow 的范围, 单位分钟
    Double rtTolerance;

    // ri窗口
    Double riTolerance;

    // 分子结构式二维图的网络url地址
    String structure2d;

    // Smiles图,从HMDB网站爬取
    String smiles;

    // 在外部数据库的ID
    String pubChemId;
    String casId;
    String keggId;
    String hmdbId;
    String chemSpiderId;

    // 用于存储外部id的字段,后面会弃用
    @Deprecated
    String outerId;

    // 暂时无用的3个保留字段
    String superPathway;
    String subPathway;
    String pathwaySortOrder;

    // 在上传库时, 根据库里面的ri和mz的距离, 得到的相近靶标列表, 用;分隔
    String neighborInfo;

    //靶标本身的分子质量,不带电
    Double avgMw;
    Double monoMw;

    String comments;
    HashMap<String, Object> statMap = new HashMap<>();

    public ExportTargetRow() {
    }

    public Object getValueByName(String fieldName) {
        try {
            return ExportTargetRow.class.getDeclaredField(fieldName).get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
