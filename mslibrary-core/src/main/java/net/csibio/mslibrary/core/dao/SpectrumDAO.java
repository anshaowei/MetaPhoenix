package net.csibio.mslibrary.core.dao;

import com.mongodb.client.result.DeleteResult;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class SpectrumDAO extends BaseDAO<SpectrumDO, SpectrumQuery> {

    public static String CollectionName = "spectrum";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return SpectrumDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(SpectrumQuery outerSpectrumQuery) {
        return new Query();
    }

    public List<SpectrumDO> getAllByCompoundId(String compoundId) {
        SpectrumQuery query = new SpectrumQuery();
        query.setCompoundId(compoundId);
        return mongoTemplate.find(buildQueryWithoutPage(query), SpectrumDO.class, CollectionName);
    }

    public List<SpectrumDO> getAllByLibraryId(String libraryId) {
        SpectrumQuery query = new SpectrumQuery();
        query.setLibraryId(libraryId);
        return mongoTemplate.find(buildQueryWithoutPage(query), SpectrumDO.class, CollectionName);
    }

    public List<SpectrumDO> getByIds(List<String> spectraIds) {
        SpectrumQuery query = new SpectrumQuery();
        query.setIds(spectraIds);
        return mongoTemplate.find(buildQueryWithoutPage(query), SpectrumDO.class, CollectionName);
    }

    public long removeAllByLibraryId(String libraryId) {
        Query query = new Query(where("libraryId").is(libraryId));
        DeleteResult result = mongoTemplate.remove(query, SpectrumDO.class, CollectionName);
        return result.getDeletedCount();
    }

    public long removeAllByCompoundId(String compoundId) {
        Query query = new Query(where("compoundId").is(compoundId));
        DeleteResult result = mongoTemplate.remove(query, SpectrumDO.class, CollectionName);
        return result.getDeletedCount();
    }

}
