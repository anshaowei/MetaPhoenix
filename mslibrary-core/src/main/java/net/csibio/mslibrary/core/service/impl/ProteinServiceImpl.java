package net.csibio.mslibrary.core.service.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.enums.LibraryType;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.ProteinDO;
import net.csibio.mslibrary.client.domain.query.ProteinQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.IMultiDAO;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.ProteinService;
import net.csibio.mslibrary.core.dao.ProteinDAO;
import net.csibio.mslibrary.client.parser.fasta.FastaParser;
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
    @Autowired
    LibraryService libraryService;

    @Override
    public long count(ProteinQuery query, String routerId) {
        return proteinDAO.count(query, routerId);
    }

    @Override
    public IMultiDAO<ProteinDO, ProteinQuery> getBaseDAO() {
        return proteinDAO;
    }

    @Override
    public void beforeInsert(ProteinDO protein, String routerId) throws XException {
        protein.setCreateDate(new Date());
        protein.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeUpdate(ProteinDO protein, String routerId) throws XException {
        protein.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeRemove(String id, String routerId) throws XException {
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

    @Override
    public void storeToDB(List<ProteinDO> proteins, String libraryId) {
        LibraryDO library = libraryService.getById(libraryId);
        if (library == null) {
            library = new LibraryDO();
            library.setName(libraryId);
            library.setType(LibraryType.Proteomics.getName());
            libraryService.insert(library);
            log.info("蛋白质镜像库不存在,已创建新的蛋白质库");
        }
        remove(new ProteinQuery(), libraryId);
        proteins.forEach(protein -> {
            fastaParser.uniprotFormat(protein);
            protein.setLibraryId(libraryId);
        });

        insert(proteins, libraryId);
        library.setCount(proteins.size());
        libraryService.update(library);
    }
}
