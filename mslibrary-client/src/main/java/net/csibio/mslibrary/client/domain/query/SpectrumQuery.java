package net.csibio.mslibrary.client.domain.query;

import lombok.Data;

import java.util.List;

@Data
public class SpectrumQuery extends PageQuery {

    private static final long serialVersionUID = -3118698391602926445L;

    String id;
    String compoundId;

    String libraryId;

    //此字段几乎等效于libraryId
    String libraryMembership;

    String spectrumId;

    String adduct;

    List<String> ids;

    List<String> compoundIds;

    public SpectrumQuery() {
    }

    public SpectrumQuery(String compoundId) {
        this.compoundId = compoundId;
    }
}
