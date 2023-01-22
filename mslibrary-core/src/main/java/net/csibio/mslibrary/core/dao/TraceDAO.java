package net.csibio.mslibrary.core.dao;

import net.csibio.mslibrary.client.domain.db.TraceDO;
import net.csibio.mslibrary.client.domain.query.TraceQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class TraceDAO extends BaseDAO<TraceDO, TraceQuery> {

    public static String CollectionName = "trace";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return TraceDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(TraceQuery targetQuery) {
        Query query = new Query();
        if (targetQuery.getId() != null) {
            query.addCriteria(where("id").is(targetQuery.getId()));
        }
        if (targetQuery.getRunId() != null) {
            query.addCriteria(where("runId").is(targetQuery.getRunId()));
        }
        if (targetQuery.getLibraryId() != null) {
            query.addCriteria(where("libraryId").is(targetQuery.getLibraryId()));
        }
        if (targetQuery.getName() != null) {
            query.addCriteria(where("name").regex(targetQuery.getName(), "i"));
        }
        if (targetQuery.getOverviewId() != null) {
            query.addCriteria(where("overviewId").is(targetQuery.getOverviewId()));
        }
        if (targetQuery.getTemplate() != null) {
            query.addCriteria(where("template").is(targetQuery.getTemplate()));
        }
        if (targetQuery.getStatusList() != null) {
            query.addCriteria(where("status").in(targetQuery.getStatusList()));
        }

        return query;
    }

}
