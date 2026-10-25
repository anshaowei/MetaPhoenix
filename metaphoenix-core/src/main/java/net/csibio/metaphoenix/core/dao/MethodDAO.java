package net.csibio.metaphoenix.core.dao;

import net.csibio.metaphoenix.client.domain.db.MethodDO;
import net.csibio.metaphoenix.client.domain.query.MethodQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class MethodDAO extends BaseDAO<MethodDO, MethodQuery> {

    public static String CollectionName = "method";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return MethodDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(MethodQuery methodQuery) {
        Query query = new Query();
        if (methodQuery.getId() != null) {
            query.addCriteria(where("id").is(methodQuery.getId()));
        }
        if (methodQuery.getName() != null) {
            query.addCriteria(where("name").regex(methodQuery.getName(), "i"));
        }
        return query;
    }
}
