package net.csibio.mslibrary.core.service.impl;

import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.query.CompoundQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.IMultiDAO;
import net.csibio.mslibrary.core.dao.CompoundDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("CompoundService")
public class CompoundServiceImpl implements CompoundService {

    @Autowired
    CompoundDAO compoundDAO;

    @Override
    public List<CompoundDO> getAllByLibraryId(String libraryId) {
        return compoundDAO.getAllByLibraryId(libraryId);
    }

    @Override
    public void removeAllByLibraryId(String libraryId) {
        compoundDAO.remove(new CompoundQuery(libraryId), libraryId);
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
