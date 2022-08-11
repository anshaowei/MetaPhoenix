package net.csibio.mslibrary.client.domain.query;

import lombok.Data;

import java.util.List;

@Data
public class CompoundQuery extends PageQuery {

    String id;

    String formula;

    List<String> ids;

    String name;

    String searchName;

    String libraryId;

    public CompoundQuery() {

    }

    public CompoundQuery(String libraryId) {
        this.libraryId = libraryId;
    }
}
