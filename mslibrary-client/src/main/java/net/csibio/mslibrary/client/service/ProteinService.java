package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.domain.query.ProteinQuery;

import java.util.HashMap;

public interface ProteinService extends BaseService<ProteinDO, ProteinQuery> {

    void buildProteins(HashMap<String, String> map, String libraryId, boolean review, String ...tags);
}
