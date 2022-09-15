package net.csibio.mslibrary.core.dao;

import com.mongodb.client.result.DeleteResult;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class SpectrumDAO extends BaseMultiDAO<SpectrumDO, SpectrumQuery> {

    public static String CollectionName = "spectrum";

    @Override
    protected String getCollectionName(String routerId) {
        if (StringUtils.isNotEmpty(routerId)) {
            return CollectionName + SymbolConst.DELIMITER + routerId;
        } else {
            return CollectionName;
        }
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
    protected Query buildQueryWithoutPage(SpectrumQuery spectrumQuery) {
        return new Query();
    }

    @Override
    public SpectrumDO insert(SpectrumDO spectrumDO, String routerId) {
        if (!mongoTemplate.collectionExists(getCollectionName(routerId))) {
            mongoTemplate.createCollection(getCollectionName(routerId));
            buildIndex(SpectrumDO.class, routerId);
        }
        mongoTemplate.insert(spectrumDO, getCollectionName(routerId));
        return spectrumDO;
    }

    @Override
    public List<SpectrumDO> insert(List<SpectrumDO> spectrumDOS, String routerId) {
        if (!mongoTemplate.collectionExists(getCollectionName(routerId))) {
            mongoTemplate.createCollection(getCollectionName(routerId));
            buildIndex(SpectrumDO.class, routerId);
        }
        mongoTemplate.insert(spectrumDOS, getCollectionName(routerId));
        return spectrumDOS;
    }

    public List<SpectrumDO> getAllByCompoundId(String compoundId, String libraryId) {
        SpectrumQuery query = new SpectrumQuery();
        query.setCompoundId(compoundId);
        return mongoTemplate.find(buildQueryWithoutPage(query), SpectrumDO.class, getCollectionName(libraryId));
    }

    public List<SpectrumDO> getAllByLibraryId(String libraryId) {
        SpectrumQuery query = new SpectrumQuery();
        query.setLibraryId(libraryId);
        return mongoTemplate.find(buildQueryWithoutPage(query), SpectrumDO.class, getCollectionName(libraryId));
    }

    public List<SpectrumDO> getByIds(List<String> spectraIds, String libraryId) {
        SpectrumQuery query = new SpectrumQuery();
        query.setIds(spectraIds);
        return mongoTemplate.find(buildQueryWithoutPage(query), SpectrumDO.class, getCollectionName(libraryId));
    }

    public long removeAllByLibraryId(String libraryId) {
        Query query = new Query(where("libraryId").is(libraryId));
        DeleteResult result = mongoTemplate.remove(query, SpectrumDO.class, getCollectionName(libraryId));
        return result.getDeletedCount();
    }

    public long removeAllByCompoundId(String compoundId, String libraryId) {
        Query query = new Query(where("compoundId").is(compoundId));
        DeleteResult result = mongoTemplate.remove(query, SpectrumDO.class, getCollectionName(libraryId));
        return result.getDeletedCount();
    }

    public List<SpectrumDO> getByPrecursorMz(Double minMz, Double maxMz, String libraryId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("precursorMz").gte(minMz).lte(maxMz));
        return mongoTemplate.find(query, SpectrumDO.class, getCollectionName(libraryId));
    }

}
