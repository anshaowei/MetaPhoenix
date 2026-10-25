package net.csibio.metaphoenix.client.service;

import net.csibio.metaphoenix.client.domain.Result;
import net.csibio.metaphoenix.client.domain.db.LibraryDO;
import net.csibio.metaphoenix.client.domain.query.LibraryQuery;

import java.io.InputStream;
import java.util.List;

public interface LibraryService extends BaseService<LibraryDO, LibraryQuery> {

    Result parseAndInsert(LibraryDO library, InputStream in, int fileFormat);

    List<LibraryDO> getAllByIds(List<String> ids);

    /**
     * Warning: this method will remove libraries
     *
     * @return
     */
    Result removeAll();

}
