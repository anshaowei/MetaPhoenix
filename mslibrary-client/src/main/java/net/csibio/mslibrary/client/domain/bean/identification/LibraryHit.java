package net.csibio.mslibrary.client.domain.bean.identification;

import lombok.Data;

@Data
public class LibraryHit {

    String compoundId;

    String spectrumId;

    String compoundName;

    String libraryName;

    boolean decoy = false;

    String precursorAdduct;

    Double precursorMz;

    String smiles;

    String inChIKey;

    String url;

    //打分
    Double score;

}
