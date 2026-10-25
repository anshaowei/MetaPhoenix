package net.csibio.metaphoenix.client.domain.query;

import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class MethodQuery extends PageQuery {

    String id;

    String name;

    public MethodQuery() {
    }

    public MethodQuery(int pageNo, int pageSize, Sort.Direction direction, String sortColumn) {
        super(pageNo, pageSize, direction, sortColumn);
    }

}
