package net.csibio.mslibrary.core.dao;

import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.query.CompoundQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return new Query();
    }

    public List<CompoundDO> getAllByLibraryId(String libraryId) {
        CompoundQuery query = new CompoundQuery();
        query.setLibraryId(libraryId);
        return mongoTemplate.find(buildQueryWithoutPage(query), CompoundDO.class, getCollectionName(libraryId));
    }

}
