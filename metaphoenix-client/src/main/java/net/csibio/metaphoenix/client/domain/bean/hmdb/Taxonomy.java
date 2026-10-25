package net.csibio.metaphoenix.client.domain.bean.hmdb;

import lombok.Data;

import java.util.List;

@Data
public class Taxonomy {

    String description;
    String directParent;
    String kingdom;
    String superClass;
    String subClass;
    String clazz;
    String molecularFramework;
    List<String> alterParents;
    List<String> substituents;
    List<String> extDesc;
}
