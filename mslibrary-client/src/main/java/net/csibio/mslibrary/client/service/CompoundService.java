package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.query.CompoundQuery;

import java.util.List;

public interface CompoundService extends BaseService<CompoundDO, CompoundQuery> {

    List<CompoundDO> getAllByLibraryId(String libraryId);

    void removeAllByLibraryId(String libraryId);
}
