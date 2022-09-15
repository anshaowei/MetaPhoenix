package net.csibio.mslibrary.client.domain.bean.identification;

import lombok.Data;

@Data
public class IdentificationInfo {

    String compoundId;
    String compoundName;
    String libraryName;
    String formula;
    String metaPathways;
    Double matchScore;
    String smiles;
    String inChI;

}
