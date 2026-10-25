package net.csibio.metaphoenix.client.domain.query;

import lombok.Data;

import java.util.List;

@Data
public class SpectrumQuery extends PageQuery {

    private static final long serialVersionUID = -3118698391602926445L;

    String id;
    String compoundId;

    String libraryId;

    String spectrumId;

    Integer msLevel;

    String compoundName;

    String ionSource;

    String instrumentType;

    String instrument;

    String precursorAdduct;

    Double precursorMz;

    Double collisionEnergy;

    String formula;

    //默认mz搜索窗口
    Double mzTolerance = 0.01;

    Double exactMass;

    String inchI;

    String ionMode;

    List<String> ids;

    List<String> compoundIds;

    public SpectrumQuery() {
    }

    public SpectrumQuery(String compoundId) {
        this.compoundId = compoundId;
    }
}
