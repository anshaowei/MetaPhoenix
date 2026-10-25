package net.csibio.metaphoenix.core.dao;

import net.csibio.aird.constant.SymbolConst;
import net.csibio.metaphoenix.client.domain.db.CompoundDO;
import net.csibio.metaphoenix.client.domain.query.CompoundQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class CompoundDAO extends BaseMultiDAO<CompoundDO, CompoundQuery> {

    public static String CollectionName = "compound";

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
        return CompoundDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(CompoundQuery compoundQuery) {
        Query query = new Query();
        if (compoundQuery.getId() != null && !compoundQuery.getId().isEmpty()) {
            query.addCriteria(where("id").is(compoundQuery.getId()));
        } else if (compoundQuery.getIds() != null && compoundQuery.getIds().size() != 0) {
            query.addCriteria(where("id").in(compoundQuery.getIds()));
        }
        if (compoundQuery.getLibraryId() != null && !compoundQuery.getLibraryId().isEmpty()) {
            query.addCriteria(where("libraryId").is(compoundQuery.getLibraryId()));
        }
        if (compoundQuery.getName() != null && !compoundQuery.getName().isEmpty()) {
            query.addCriteria(where("name").is(compoundQuery.getName()));
        }
        if (compoundQuery.getFormula() != null && !compoundQuery.getFormula().isEmpty()) {
            query.addCriteria(where("formula").is(compoundQuery.getFormula()));
        }
        if (compoundQuery.getState() != null && !compoundQuery.getState().isEmpty()) {
            query.addCriteria(where("state").is(compoundQuery.getState()));
        }
        if (compoundQuery.getStatus() != null && !compoundQuery.getStatus().isEmpty()) {
            query.addCriteria(where("status").is(compoundQuery.getStatus()));
        }
        return query;
    }

    public List<CompoundDO> getAllByLibraryId(String libraryId) {
        CompoundQuery query = new CompoundQuery();
        query.setLibraryId(libraryId);
        return mongoTemplate.find(buildQueryWithoutPage(query), CompoundDO.class, getCollectionName(libraryId));
    }

    @Override
    public CompoundDO insert(CompoundDO compoundDO, String routerId) {
        if (!mongoTemplate.collectionExists(getCollectionName(routerId))) {
            mongoTemplate.createCollection(getCollectionName(routerId));
            buildIndex(CompoundDO.class, routerId);
        }
        mongoTemplate.insert(compoundDO, getCollectionName(routerId));
        return compoundDO;
    }

    @Override
    public List<CompoundDO> insert(List<CompoundDO> compoundDOS, String routerId) {
        if (!mongoTemplate.collectionExists(getCollectionName(routerId))) {
            mongoTemplate.createCollection(getCollectionName(routerId));
            buildIndex(CompoundDO.class, routerId);
        }
        mongoTemplate.insert(compoundDOS, getCollectionName(routerId));
        return compoundDOS;
    }

}
