package net.csibio.mslibrary.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.IMultiDAO;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.core.dao.LibraryDAO;
import net.csibio.mslibrary.core.dao.SpectrumDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("spectrumService")
public class SpectrumServiceImpl implements SpectrumService {

    public final Logger logger = LoggerFactory.getLogger(SpectrumServiceImpl.class);

    @Autowired
    SpectrumDAO spectrumDAO;

    @Autowired
    LibraryDAO libraryDAO;

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public long count(SpectrumQuery query, String libraryId) {
        return spectrumDAO.count(query, libraryId);
    }

    @Override
    public IMultiDAO<SpectrumDO, SpectrumQuery> getBaseDAO() {
        return spectrumDAO;
    }

    @Override
    public void beforeInsert(SpectrumDO spectrum, String routerId) throws XException {

    }

    @Override
    public void beforeUpdate(SpectrumDO spectrum, String routerId) throws XException {

    }

    @Override
    public void beforeRemove(String id, String routerId) throws XException {

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
    public List<SpectrumDO> getAllByCompoundId(String compoundId, String libraryId) {
        try {
            return spectrumDAO.getAllByCompoundId(compoundId, libraryId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public List<SpectrumDO> getByPrecursorMz(Double precursorMz, String libraryId) {
        return spectrumDAO.getByPrecursorMz(precursorMz, libraryId);
    }

    /**
     * 高危操作，移除所有谱图及谱图集合
     *
     * @return
     */
    @Override
    public Result removeAll() {
        LibraryQuery libraryQuery = new LibraryQuery();
        List<LibraryDO> libraryDOList = libraryDAO.getAll(libraryQuery);
        for (LibraryDO libraryDO : libraryDOList) {
            SpectrumQuery spectrumQuery = new SpectrumQuery();
            spectrumDAO.remove(spectrumQuery, libraryDO.getId());
            mongoTemplate.dropCollection("spectrum" + SymbolConst.DELIMITER + libraryDO.getId());
            log.info("已经移除库" + libraryDO.getName() + "的所有谱图");
        }
        log.info("已经移除所有谱图及集合");
        return null;
    }

    @Override
    public Result<List<SpectrumDO>> insert(List<SpectrumDO> spectrumDOS, String libraryId) {
        spectrumDAO.insert(spectrumDOS, libraryId);
        return new Result(true);
    }

    @Override
    public Result<SpectrumDO> insert(SpectrumDO spectrumDO, String libraryId) {
        spectrumDAO.insert(spectrumDO, libraryId);
        return new Result<>(true);
    }
}
