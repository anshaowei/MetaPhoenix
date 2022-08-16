package net.csibio.mslibrary.core.service.impl;

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
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("CompoundService")
public class CompoundServiceImpl implements CompoundService {

    @Autowired
    CompoundDAO compoundDAO;

    @Autowired
    LibraryDAO libraryDAO;

    @Override
    public List<CompoundDO> getAllByLibraryId(String libraryId) {
        return compoundDAO.getAllByLibraryId(libraryId);
    }

    @Override
    public Result removeAllByLibraryId(String libraryId) {
        compoundDAO.remove(new CompoundQuery(libraryId), libraryId);
        return new Result(true);
    }

    @Override
    public Result removeAll() {
        CompoundQuery query = new CompoundQuery();
        LibraryQuery libraryQuery = new LibraryQuery();
        List<LibraryDO> libraryDOList = libraryDAO.getAll(libraryQuery);
        for (LibraryDO libraryDO : libraryDOList) {
            compoundDAO.remove(query, libraryDO.getId());
        }
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

}
