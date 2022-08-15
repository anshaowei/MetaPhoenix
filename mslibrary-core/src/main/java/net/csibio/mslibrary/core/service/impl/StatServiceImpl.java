package net.csibio.mslibrary.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.LibraryConst;
import net.csibio.mslibrary.client.constants.StatConst;
import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.constants.enums.StatDim;
import net.csibio.mslibrary.client.constants.enums.StatType;
import net.csibio.mslibrary.client.domain.db.StatDO;
import net.csibio.mslibrary.client.domain.query.*;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.*;
import net.csibio.mslibrary.core.dao.StatDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@Slf4j
@Service("statService")
public class StatServiceImpl implements StatService {

    @Autowired
    StatDAO statDAO;
    @Autowired
    LibraryService libraryService;
    @Autowired
    CompoundService compoundService;
    @Autowired
    SpectrumService spectrumService;

    @Autowired
    ProteinService proteinService;
    @Autowired
    GeneService geneService;

    @Override
    public long count(StatQuery query) {
        return statDAO.count(query);
    }

    @Override
    public IDAO<StatDO, StatQuery> getBaseDAO() {
        return statDAO;
    }

    @Override
    public void beforeInsert(StatDO stat) throws XException {
        stat.setCreateDate(new Date());
        stat.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeUpdate(StatDO stat) throws XException {
        stat.setLastModifiedDate(new Date());
    }

    @Override
    public StatDO getByUniqueKey(String dim, String type, Date date) {
        StatQuery query = new StatQuery(dim, type, date);
        return getOne(query, StatDO.class);
    }

    @Override
    public StatDO getByUniqueKey(String dim, String type, String dateStr) throws XException {
        SimpleDateFormat sdf = new SimpleDateFormat(StatDO.DATE_FORMAT);
        try {
            Date date = sdf.parse(dateStr);
            return getByUniqueKey(dim, type, date);
        } catch (ParseException e) {
            throw new XException(ResultCode.DATE_TIME_FORMAT_ERROR);
        }
    }

    @Override
    public void globalStat(StatDim dim, Date date) {
        StatDO stat = getByUniqueKey(dim.getDim(), StatType.Global_Total.getName(), date);
        if (stat != null) {
            remove(stat.getId());
        }
        stat = new StatDO(dim.getDim(), StatType.Global_Total.getName(), date);

        long libraryNum = libraryService.count(new LibraryQuery());

        long hmdbGeneNum = geneService.count(new GeneQuery(LibraryConst.HMDB_GENE));
        long hmdbProteinNum = proteinService.count(new ProteinQuery(LibraryConst.HMDB_PROTEIN));
        long hmdbCompoundNum = compoundService.count(new CompoundQuery(), LibraryConst.HMDB_COMPOUND);

        long uniprotGeneNum = geneService.count(new GeneQuery(LibraryConst.UNIPROT_GENE));
        long uniprotProteinNum = proteinService.count(new ProteinQuery(LibraryConst.UNIPROT_PROTEIN));

        long gnpsNum = compoundService.count(new CompoundQuery(), LibraryConst.GNPS);
        long massBankNum = compoundService.count(new CompoundQuery(), LibraryConst.MassBank);
        long mslibraryNum = compoundService.count(new CompoundQuery(), LibraryConst.Empty);
        long spectraNum = spectrumService.count(new SpectrumQuery());

        HashMap<String, Object> statMap = new HashMap<>();
        statMap.put(StatConst.STAT_LIBRARY_NUM, libraryNum);
        statMap.put(StatConst.STAT_HMDB_GENE_NUM, hmdbGeneNum);
        statMap.put(StatConst.STAT_HMDB_PROTEIN_NUM, hmdbProteinNum);
        statMap.put(StatConst.STAT_HMDB_COMPOUND_NUM, hmdbCompoundNum);

        statMap.put(StatConst.STAT_UNIPROT_GENE_NUM, uniprotGeneNum);
        statMap.put(StatConst.STAT_UNIPROT_PROTEIN_NUM, uniprotProteinNum);

        statMap.put(StatConst.STAT_GNPS_COMPOUND_NUM, gnpsNum);
        statMap.put(StatConst.STAT_MASSBANK_COMPOUND_NUM, massBankNum);
        statMap.put(StatConst.STAT_MSLIBRARY_COMPOUND_NUM, mslibraryNum);

        stat.setStatMap(statMap);
        statDAO.insert(stat);
    }
}
