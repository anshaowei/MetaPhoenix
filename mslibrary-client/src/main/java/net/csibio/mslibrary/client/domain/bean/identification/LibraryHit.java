package net.csibio.mslibrary.client.domain.bean.identification;

import lombok.Data;

@Data
public class LibraryHit {

    String compoundId;

    String spectrumId;

    String compoundName;

    String libraryName;

    String adduct;

    Double precursorMz;

    String smiles;

    String inChI;

    String url;

    //打分
    Double matchScore;

}
