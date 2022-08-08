package net.csibio.mslibrary.client.domain.query;

import lombok.Data;

@Data
public class CompoundQuery extends PageQuery {

    String id;

    String name;

    String libraryId;

    public CompoundQuery() {

    }

    public CompoundQuery(String libraryId) {
        this.libraryId = libraryId;
    }
}
