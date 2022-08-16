package net.csibio.mslibrary.core.dao;

import net.csibio.aird.bean.common.IdName;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class LibraryDAO extends BaseDAO<LibraryDO, LibraryQuery> {

    public static String CollectionName = "library";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return LibraryDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(LibraryQuery libraryQuery) {
        Query query = new Query();
        if (libraryQuery.getId() != null) {
            query.addCriteria(where("id").is(libraryQuery.getId()));
        }
        if (libraryQuery.getName() != null) {
            query.addCriteria(where("name").regex(libraryQuery.getName(), "i"));
        }
        if (libraryQuery.getType() != null) {
            query.addCriteria(where("type").is(libraryQuery.getType()));
        }
        if (libraryQuery.getPlatform() != null) {
            query.addCriteria(where("platform").is(libraryQuery.getPlatform()));
        }
        if (libraryQuery.getSpecies() != null) {
            query.addCriteria(where("species").in(libraryQuery.getSpecies()));
        }
        if (libraryQuery.getMatrix() != null) {
            query.addCriteria(where("matrix").in(libraryQuery.getMatrix()));
        }
        if (libraryQuery.getCreateDateStart() != null && libraryQuery.getCreateDateEnd() != null) {
            query.addCriteria(where("createDate").gte(libraryQuery.getCreateDateStart()).lt(libraryQuery.getCreateDateEnd()));
        }
        if (allowSort()) {
            if (libraryQuery.getSortColumn() != null && libraryQuery.getOrderBy() != null) {
                query.with(Sort.by(libraryQuery.getOrderBy(), libraryQuery.getSortColumn()));
            }
        }
        return query;
    }

    /**
     * 修改库下的靶标的数目
     *
     * @param libraryId 需要修改的库id
     * @param delta     修改的数目,负数表示减少,正数表示增加
     * @return
     */
    public LibraryDO modifyTargetCount(String libraryId, Number delta) {
        Query query = new Query(where("id").is(libraryId));
        Update update = new Update();
        update.inc("targetCount", delta);
        LibraryDO lib = mongoTemplate.findAndModify(query, update, LibraryDO.class, CollectionName);
        return lib;
    }

    public List<IdName> getIdNameList(LibraryQuery query) {
        return mongoTemplate.find(buildQueryWithoutPage(query), IdName.class, CollectionName);
    }


}
