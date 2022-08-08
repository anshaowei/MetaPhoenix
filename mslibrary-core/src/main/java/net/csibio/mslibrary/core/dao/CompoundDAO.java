package net.csibio.mslibrary.core.dao;

import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.query.CompoundQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompoundDAO extends BaseDAO<CompoundDO, CompoundQuery> {

    public static String CollectionName = "compound";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return CompoundDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(CompoundQuery compoundQuery) {
        return new Query();
    }

    public List<CompoundDO> getAllByOuterLibraryId(String outerLibraryId) {
        CompoundQuery query = new CompoundQuery();
        query.setLibraryId(outerLibraryId);
        return mongoTemplate.find(buildQueryWithoutPage(query), CompoundDO.class, CollectionName);
    }

}
