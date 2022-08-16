package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;

import java.io.InputStream;

public interface LibraryService extends BaseService<LibraryDO, LibraryQuery> {

    Result parseAndInsert(LibraryDO library, InputStream in, int fileFormat);

}
