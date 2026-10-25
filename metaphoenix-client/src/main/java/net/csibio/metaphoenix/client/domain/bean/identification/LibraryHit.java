package net.csibio.metaphoenix.client.domain.bean.identification;

import lombok.Data;

@Data
public class LibraryHit {

    String querySpectrumId;

    String libSpectrumId;

    String compoundName;

    String libraryId;

    boolean isDecoy = false;

    boolean right = false;

    String precursorAdduct;

    Double precursorMz;

    String smiles;

    String inChIKey;

    Double score;

}
