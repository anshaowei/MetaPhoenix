package net.csibio.mslibrary.client.domain.query;

import lombok.Data;

import java.io.Serial;
import java.util.Date;

@Data
public class StatQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = -3288364533466853225L;

    String id;

    String dim;

    String type;

    Date date;

    Date dateStart;

    Date dateEnd;

    public StatQuery() {}

    public StatQuery(String dim, String type, Date date) {
        this.dim = dim;
        this.type = type;
        this.date = date;
    }
}
