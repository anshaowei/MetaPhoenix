package net.csibio.metaphoenix.client.domain.query;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class LibraryQuery extends PageQuery {

    String id;

    String name;

    String type;

    String platform;

    List<String> matrix;

    List<String> species;

    List<String> ids;

    Date createDateStart;

    Date createDateEnd;

    public LibraryQuery() {
    }

    public LibraryQuery(Date from, Date to) {
        this.createDateStart = from;
        this.createDateEnd = to;
    }

    public void addSpecies(String species) {
        if (this.species == null) {
            this.species = new ArrayList<>();
        }
        if (!this.species.contains(species)) {
            this.species.add(species);
        }
    }

    public void addMatrix(String matrix) {
        if (this.matrix == null) {
            this.matrix = new ArrayList<>();
        }
        if (!this.matrix.contains(matrix)) {
            this.matrix.add(matrix);
        }
    }
}
