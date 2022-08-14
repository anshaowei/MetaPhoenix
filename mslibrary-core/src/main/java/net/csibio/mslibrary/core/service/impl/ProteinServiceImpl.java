package net.csibio.mslibrary.core.service.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.domain.query.ProteinQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.*;
import net.csibio.mslibrary.core.dao.LibraryDAO;
import net.csibio.mslibrary.core.dao.ProteinDAO;
import net.csibio.mslibrary.core.parser.fasta.FastaParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
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
    public void buildProteins(HashMap<String, String> map, String libraryId, boolean review, String ...tags) {
        List<ProteinDO> proteinDOList = new ArrayList<>();
        for (String key : map.keySet()) {
            ProteinDO protein = new ProteinDO();
            protein.setLibraryId(libraryId);
            StringBuilder keyBuilder = new StringBuilder(key);
            keyBuilder.delete(0, 1);
            String firstLine = keyBuilder.toString();
            String newString = keyBuilder.toString();
            String[] s = firstLine.split(SymbolConst.SPACE);
            String newS = StringUtils.substringAfter(firstLine, s[0]);
            String name = StringUtils.substringBefore(newS, "OS=");
            String identifier = StringUtils.substringBefore(firstLine, "OS=");
            protein.setId(identifier);
            String os = StringUtils.substringBetween(firstLine, "OS=", "OX=");
            String gn = StringUtils.substringBetween(newString, "GN=", "PE=");
            protein.setGene(gn);
            protein.setOrganism(os);
            protein.setTags(Lists.newArrayList(tags));
            protein.setId(s[0]);
            protein.setReviewed(review);
            String substringName = name.substring(1, name.length() - 1);
            List<String> nameList = new ArrayList<>();
            nameList.add(substringName);
            protein.setNames(nameList);
            protein.setSequence(map.get(key));
            proteinDOList.add(protein);
        }
        insert(proteinDOList);
    }
}
