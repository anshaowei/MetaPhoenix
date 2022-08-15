package net.csibio.mslibrary.core.service.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.db.GeneDO;
import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.domain.query.GeneQuery;
import net.csibio.mslibrary.client.domain.query.ProteinQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.GeneService;
import net.csibio.mslibrary.client.service.IDAO;
import net.csibio.mslibrary.client.service.ProteinService;
import net.csibio.mslibrary.core.dao.GeneDAO;
import net.csibio.mslibrary.core.dao.ProteinDAO;
import net.csibio.mslibrary.core.parser.fasta.FastaParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service("geneService")
public class GeneServiceImpl implements GeneService {

    @Autowired
    GeneDAO geneDAO;
    @Autowired
    FastaParser fastaParser;

    @Override
    public long count(GeneQuery query) {
        return geneDAO.count(query);
    }

    @Override
    public IDAO<GeneDO, GeneQuery> getBaseDAO() {
        return geneDAO;
    }

    @Override
    public void beforeInsert(GeneDO gene) throws XException {
        gene.setCreateDate(new Date());
        gene.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeUpdate(GeneDO gene) throws XException {
        gene.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeRemove(String id) throws XException {
    }

    @Override
    public List<GeneDO> buildGenes(HashMap<String, String> map, String... tags) {
        List<GeneDO> geneList = new ArrayList<>();
        for (String key : map.keySet()) {
            GeneDO gene = new GeneDO();
            gene.setIdentifyLine(key);
            gene.setTags(Lists.newArrayList(tags));
            gene.setSequence(map.get(key));
            geneList.add(gene);
        }
        return geneList;
    }
}
