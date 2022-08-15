package net.csibio.mslibrary.client.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProteinUpdateVO {

    String id;

    String libraryId;

    String identifyLine;

    String uniprot;

    Boolean reviewed;

    List<String> names;

    List<String> tags;

    String organism;

    String gene;

    String sequence;
}
