package net.csibio.mslibrary.client.domain.query;

import lombok.Data;

@Data
public class GeneQuery extends PageQuery {

    String id;

    String libraryId;

    String organism;

    String gene;

    public GeneQuery() {

    }

    public GeneQuery(String libraryId) {
        this.libraryId = libraryId;
    }
}
