package net.csibio.mslibrary.client.service;


import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;

import java.util.List;

public interface OuterSpectrumService extends BaseService<SpectrumDO, SpectrumQuery> {

    List<SpectrumDO> getAllByCompound(String compoundId);

    Result insertAll(List<SpectrumDO> outerSpectrumDOS);

}
