package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;

import java.util.ArrayList;
import java.util.List;

public interface SpectrumService extends BaseService<SpectrumDO, SpectrumQuery> {

    List<SpectrumDO> getAllByLibraryId(String libraryId);

    List<SpectrumDO> getAllByCompoundId(String compoundId);

    Result insertAll(List<SpectrumDO> spectrumDOS);


}

