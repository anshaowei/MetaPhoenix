package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.query.CompoundQuery;

import java.util.List;

public interface CompoundService extends BaseMultiService<CompoundDO, CompoundQuery> {

    List<CompoundDO> getAllByLibraryId(String libraryId);

    Result removeAllByLibraryId(String libraryId);

    Result removeAll();
}
