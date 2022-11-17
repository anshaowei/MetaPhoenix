package net.csibio.mslibrary.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.*;
import net.csibio.mslibrary.core.dao.LibraryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

@Slf4j
@Service("libraryService")
public class LibraryServiceImpl implements LibraryService {

    @Autowired
    LibraryDAO libraryDAO;
    @Autowired
    LibraryParserService libraryParserService;
    @Autowired
    CompoundService compoundService;
    @Autowired
    SpectrumService spectrumService;

    @Override
    public long count(LibraryQuery query) {
        return libraryDAO.count(query);
    }

    @Override
    public IDAO<LibraryDO, LibraryQuery> getBaseDAO() {
        return libraryDAO;
    }

    @Override
    public void beforeInsert(LibraryDO library) throws XException {
        library.setCreateDate(new Date());
        library.setLastModifiedDate(new Date());
        library.setId(library.getName());
    }

    @Override
    public void beforeUpdate(LibraryDO library) throws XException {
        library.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeRemove(String id) throws XException {
        compoundService.removeAllByLibraryId(id);
    }

    @Override
    public Result parseAndInsert(LibraryDO library, InputStream in, int fileFormat) {
        Result result = null;
        try {
            result = libraryParserService.parse(in, library, fileFormat);
        } catch (Exception e) {
            libraryDAO.remove(library.getId());
            return Result.Error(e.getMessage());
        }
        return result;
    }

    @Override
    public List<LibraryDO> getAllByIds(List<String> ids) {
        try {
            return libraryDAO.getAllByIds(ids);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Result removeAll() {
        libraryDAO.remove(new LibraryQuery());
        log.info("所有库已经被删除");
        return new Result(true);
    }
}
