package net.csibio.metaphoenix.client.domain.bean.hmdb;

import lombok.Data;

import java.util.List;

@Data
public class Descendant {

    String term;
    String definition;
    String parentId;
    Integer level;
    String type;
    List<String> synonyms;
    List<Descendant> descendants;
}
