package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.domain.query.ProteinQuery;

import java.util.HashMap;
import java.util.List;

public interface ProteinService extends BaseMultiService<ProteinDO, ProteinQuery> {

    List<ProteinDO> buildProteins(HashMap<String, String> map, String ...tags);
}
