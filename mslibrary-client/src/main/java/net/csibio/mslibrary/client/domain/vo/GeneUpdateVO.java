package net.csibio.mslibrary.client.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class GeneUpdateVO {

    String id;

    String libraryId;

    String identifyLine;

    List<String> names;

    List<String> tags;

    String organism;

    String gene;

    String sequence;
}
