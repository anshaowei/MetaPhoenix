package net.csibio.mslibrary.client.domain.query;

import lombok.Data;

import java.util.List;

@Data
public class SpectrumQuery extends PageQuery {

    private static final long serialVersionUID = -3118698391602926445L;

    String id;

    String targetId;

    String innerId;

    String platform;

    List<String> ids;

    List<String> targetIds;

    Boolean root;

    String libraryId;

    String source;

    String type;

    List<String> overviewIds;

    public SpectrumQuery() {
    }

    public SpectrumQuery(String targetId) {
        this.targetId = targetId;
    }
}
