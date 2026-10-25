package net.csibio.metaphoenix.client.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class CompoundUpdateVO {

    String id;

    String libraryId;

    String code;

    String name;

    String cnName;

    Double mz;

    Double ri;

    String formula;

    String type;

    Double delta;

    Integer charge;

    String mainAdduct;

//     关联的加和物列表
//    HashSet<Adduct> adducts = new HashSet<>();

    Double rt;

    Double rtTolerance;

    Double riTolerance;

    String structure2d;

    String smiles;

    String pubChemId;
    String casId;
    String drugBankId;
    String keggId;
    String hmdbId;
    String chemSpiderId;

    String superPathway;
    String subPathway;
    String pathwaySortOrder;

    // 在上传库时, 根据库里面的ri和mz的距离, 得到的相近靶标列表, 用;分隔
    List<String> neighbors;

    //靶标本身的分子质量,不带电
    Double avgMw;
    Double monoMw;

    String comments;
}
