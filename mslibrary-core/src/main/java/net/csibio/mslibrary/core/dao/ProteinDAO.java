package net.csibio.mslibrary.core.dao;

import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.domain.query.ProteinQuery;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class ProteinDAO extends BaseDAO<ProteinDO, ProteinQuery> {

    public static String CollectionName = "protein";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return ProteinDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(ProteinQuery proteinQuery) {
        Query query = new Query();
        if (proteinQuery.getId() != null) {
            query.addCriteria(where("id").is(proteinQuery.getId()));
        }
        if (proteinQuery.getLibraryId() != null) {
            query.addCriteria(where("libraryId").is(proteinQuery.getLibraryId()));
        }
        if (proteinQuery.getGene() != null) {
            query.addCriteria(where("gene").is(proteinQuery.getGene()));
        }
        if (proteinQuery.getReviewed() != null) {
            query.addCriteria(where("reviewed").is(proteinQuery.getReviewed()));
        }
        if (proteinQuery.getOrganism() != null) {
            query.addCriteria(where("organism").is(proteinQuery.getOrganism()));
        }
        if (allowSort()) {
            if (proteinQuery.getSortColumn() != null && proteinQuery.getOrderBy() != null) {
                query.with(Sort.by(proteinQuery.getOrderBy(), proteinQuery.getSortColumn()));
            }
        }
        return query;
    }
}
