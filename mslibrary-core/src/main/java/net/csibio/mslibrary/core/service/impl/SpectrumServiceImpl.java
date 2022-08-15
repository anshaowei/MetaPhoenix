package net.csibio.mslibrary.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.IDAO;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.core.dao.SpectrumDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service("spectrumService")
public class SpectrumServiceImpl implements SpectrumService {

    public final Logger logger = LoggerFactory.getLogger(SpectrumServiceImpl.class);

    @Autowired
    SpectrumDAO spectrumDAO;

    @Override
    public long count(SpectrumQuery query) {
        return spectrumDAO.count(query);
    }

    @Override
    public IDAO<SpectrumDO, SpectrumQuery> getBaseDAO() {
        return spectrumDAO;
    }

    @Override
    public void beforeInsert(SpectrumDO spectrum) throws XException {
        spectrum.setCreateDate(new Date());
        spectrum.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeUpdate(SpectrumDO spectrum) throws XException {
        spectrum.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeRemove(String id) throws XException {

    }

    @Override
    public List<SpectrumDO> getAllByLibraryId(String libraryId) {
        try {
            return spectrumDAO.getAllByLibraryId(libraryId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public List<SpectrumDO> getAllByCompoundId(String compoundId) {
        try {
            return spectrumDAO.getAllByCompoundId(compoundId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}
