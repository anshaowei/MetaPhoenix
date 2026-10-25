package net.csibio.metaphoenix.client.domain.bean.hmdb;

import lombok.Data;

@Data
public class ProteinAssociation {
    String proteinAccession;
    String name;
    String uniprotId;
    String geneName;
    String proteinType;
}
