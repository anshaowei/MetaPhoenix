package net.csibio.mslibrary.core.dao;

import net.csibio.mslibrary.client.domain.db.StatDO;
import net.csibio.mslibrary.client.domain.query.StatQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class StatDAO extends BaseDAO<StatDO, StatQuery> {

    public static String CollectionName = "stat";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return StatDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(StatQuery statQuery) {
        Query query = new Query();
        if (statQuery.getId() != null) {
            query.addCriteria(where("id").is(statQuery.getId()));
        }
        if (statQuery.getDim() != null) {
            query.addCriteria(where("dim").is(statQuery.getDim()));
        }
        if (statQuery.getType() != null) {
            query.addCriteria(where("type").is(statQuery.getType()));
        }
        if (statQuery.getDate() != null) {
            query.addCriteria(where("date").is(statQuery.getDate()));
        } else if (statQuery.getDateStart() != null && statQuery.getDateEnd() != null) {
            query.addCriteria(where("date").gte(statQuery.getDateStart()).lt(statQuery.getDateEnd()));
        }
        return query;
    }
}
