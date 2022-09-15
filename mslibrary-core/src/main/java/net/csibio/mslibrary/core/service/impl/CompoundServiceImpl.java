package net.csibio.mslibrary.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.query.CompoundQuery;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.IMultiDAO;
import net.csibio.mslibrary.core.dao.CompoundDAO;
import net.csibio.mslibrary.core.dao.LibraryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("CompoundService")
public class CompoundServiceImpl implements CompoundService {

    @Autowired
    CompoundDAO compoundDAO;

    @Autowired
    LibraryDAO libraryDAO;

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<CompoundDO> getAllByLibraryId(String libraryId) {
        return compoundDAO.getAllByLibraryId(libraryId);
    }

    @Override
    public Result removeAllByLibraryId(String libraryId) {
        compoundDAO.remove(new CompoundQuery(libraryId), libraryId);
        return new Result(true);
    }

    /**
     * 高危操作，会清除数据库中所有化合物和化合物库
     *
     * @return
     */
    @Override
    public Result removeAll() {
        CompoundQuery query = new CompoundQuery();
        LibraryQuery libraryQuery = new LibraryQuery();
        List<LibraryDO> libraryDOList = libraryDAO.getAll(libraryQuery);
        for (LibraryDO libraryDO : libraryDOList) {
            compoundDAO.remove(query, libraryDO.getId());
            mongoTemplate.dropCollection("compound" + SymbolConst.DELIMITER + libraryDO.getId());
        }
        log.info("全部化合物及化合物集合清除已经完成");
        return new Result(true);
    }

    @Override
    public IMultiDAO<CompoundDO, CompoundQuery> getBaseDAO() {
        return compoundDAO;
    }

    @Override
    public void beforeInsert(CompoundDO compoundDO, String routerId) throws XException {
        compoundDO.encode();
    }

    @Override
    public void beforeUpdate(CompoundDO compoundDO, String routerId) throws XException {

    }

    @Override
    public void beforeRemove(String id, String routerId) throws XException {

    }

    @Override
    public Result<List<CompoundDO>> insert(List<CompoundDO> compoundDOS, String routerId) {
        compoundDAO.insert(compoundDOS, routerId);
        return new Result<>(true);
    }

    @Override
    public Result<CompoundDO> insert(CompoundDO compoundDO, String routerId) {
        compoundDAO.insert(compoundDO, routerId);
        return new Result<>(true);
    }

}
