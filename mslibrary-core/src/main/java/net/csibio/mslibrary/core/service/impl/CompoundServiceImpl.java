package net.csibio.mslibrary.core.service.impl;

import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.query.CompoundQuery;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.IDAO;
import net.csibio.mslibrary.core.dao.CompoundDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("CompoundService")
public class CompoundServiceImpl implements CompoundService {

    @Autowired
    CompoundDAO compoundDAO;

    @Override
    public List<CompoundDO> getAllByLibraryId(String outerLibraryId) {
        return compoundDAO.getAllByOuterLibraryId(outerLibraryId);
    }

    @Override
    public void removeAllByLibraryId(String libraryId) {
        compoundDAO.remove(new CompoundQuery(libraryId));
    }

    @Override
    public IDAO<CompoundDO, CompoundQuery> getBaseDAO() {
        return compoundDAO;
    }

}
