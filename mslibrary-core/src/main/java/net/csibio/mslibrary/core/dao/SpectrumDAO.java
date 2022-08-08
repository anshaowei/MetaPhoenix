package net.csibio.mslibrary.core.dao;

import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

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
}
