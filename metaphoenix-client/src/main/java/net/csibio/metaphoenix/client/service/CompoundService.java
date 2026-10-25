package net.csibio.metaphoenix.client.service;

import net.csibio.metaphoenix.client.domain.Result;
import net.csibio.metaphoenix.client.domain.db.CompoundDO;
import net.csibio.metaphoenix.client.domain.query.CompoundQuery;

import java.util.List;

public interface CompoundService extends BaseMultiService<CompoundDO, CompoundQuery> {

    List<CompoundDO> getAllByLibraryId(String libraryId);

    Result removeAllByLibraryId(String libraryId);

    Result removeAll();
}
