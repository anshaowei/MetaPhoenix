package net.csibio.mslibrary.core.dao;

import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.domain.db.GeneDO;
import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.domain.query.GeneQuery;
import net.csibio.mslibrary.client.domain.query.ProteinQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class GeneDAO extends BaseMultiDAO<GeneDO, GeneQuery> {

    public static String CollectionName = "gene";

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
        return GeneDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(GeneQuery geneQuery) {
        Query query = new Query();
        if (geneQuery.getId() != null) {
            query.addCriteria(where("id").is(geneQuery.getId()));
        }
        if (geneQuery.getLibraryId() != null) {
            query.addCriteria(where("libraryId").is(geneQuery.getLibraryId()));
        }
        if (geneQuery.getOrganism() != null) {
            query.addCriteria(where("organism").is(geneQuery.getOrganism()));
        }
        if (allowSort()) {
            if (geneQuery.getSortColumn() != null && geneQuery.getOrderBy() != null) {
                query.with(Sort.by(geneQuery.getOrderBy(), geneQuery.getSortColumn()));
            }
        }
        return query;
    }
}
