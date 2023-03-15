package net.csibio.mslibrary.client.domain.bean.identification;

import lombok.Data;

@Data
public class LibraryHit {

    String querySpectrumId;

    String libSpectrumId;

    String compoundName;

    String libraryId;

    boolean isDecoy = false;

    String precursorAdduct;

    Double precursorMz;

    String smiles;

    String inChIKey;

    Double score;

}
