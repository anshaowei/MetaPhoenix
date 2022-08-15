package net.csibio.mslibrary.core.service.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.domain.query.ProteinQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.*;
import net.csibio.mslibrary.core.dao.ProteinDAO;
import net.csibio.mslibrary.core.parser.fasta.FastaParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service("proteinService")
public class ProteinServiceImpl implements ProteinService {

    @Autowired
    ProteinDAO proteinDAO;
    @Autowired
    FastaParser fastaParser;

    @Override
    public long count(ProteinQuery query) {
        return proteinDAO.count(query);
    }

    @Override
    public IDAO<ProteinDO, ProteinQuery> getBaseDAO() {
        return proteinDAO;
    }

    @Override
    public void beforeInsert(ProteinDO protein) throws XException {
        protein.setCreateDate(new Date());
        protein.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeUpdate(ProteinDO protein) throws XException {
        protein.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeRemove(String id) throws XException {
    }

    @Override
    public List<ProteinDO> buildProteins(HashMap<String, String> map, String... tags) {
        List<ProteinDO> proteins = new ArrayList<>();
        for (String key : map.keySet()) {
            ProteinDO protein = new ProteinDO();
            protein.setIdentifyLine(key);
            protein.setTags(Lists.newArrayList(tags));
            protein.setSequence(map.get(key));
            proteins.add(protein);
        }
        return proteins;
    }
}
