package net.csibio.mslibrary.client.domain.query;

import lombok.Data;

@Data
public class ProteinQuery extends PageQuery {

    String id;

    String libraryId;

    String organism;

    String gene;

    Boolean reviewed;

    String createTag;

    public ProteinQuery() {

    }

    public ProteinQuery(String libraryId) {
        this.libraryId = libraryId;
    }
}
