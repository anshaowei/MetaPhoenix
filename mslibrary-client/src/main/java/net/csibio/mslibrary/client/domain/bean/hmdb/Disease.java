package net.csibio.mslibrary.client.domain.bean.hmdb;

import lombok.Data;

import java.util.List;

@Data
public class Disease {

    String name;
    String omimId;
    List<Reference> references;

}
