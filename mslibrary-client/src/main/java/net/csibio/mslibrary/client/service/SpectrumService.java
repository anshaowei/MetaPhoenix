package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;

import java.util.List;

public interface SpectrumService extends BaseMultiService<SpectrumDO, SpectrumQuery> {

    List<SpectrumDO> getAllByLibraryId(String libraryId);

    List<SpectrumDO> getAllByCompoundId(String compoundId, String libraryId);

    List<SpectrumDO> getByPrecursorMz(Double minMz, Double maxMz, String libraryId);

    Result removeAll();

}

