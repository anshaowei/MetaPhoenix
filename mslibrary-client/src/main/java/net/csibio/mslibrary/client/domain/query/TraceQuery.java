package net.csibio.mslibrary.client.domain.query;

import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@Data
public class TraceQuery extends PageQuery {

    String id;

    String name;

    String runId;

    String libraryId;

    String overviewId;

    String currentStep;

    String template;

    List<String> statusList;

    public void setStatus(String status) {
        if (statusList == null) {
            statusList = new ArrayList<>();
        } else {
            statusList.clear();
        }
        statusList.add(status);
    }

    public void addStatus(String status) {
        if (statusList == null) {
            statusList = new ArrayList<>();
        }
        statusList.add(status);
    }

    public void clearStatus() {
        if (statusList == null) {
            return;
        }
        statusList.clear();
    }

    public TraceQuery() {
    }

    public TraceQuery(int pageNo, int pageSize, Sort.Direction direction, String sortColumn) {
        super(pageNo, pageSize, direction, sortColumn);
    }
}
