package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.domain.db.GeneDO;
import net.csibio.mslibrary.client.domain.query.GeneQuery;

import java.util.HashMap;
import java.util.List;

public interface GeneService extends BaseMultiService<GeneDO, GeneQuery> {

    List<GeneDO> buildGenes(HashMap<String, String> map, String ...tags);

    void storeToDB(List<GeneDO> genes, String libraryId);
}
