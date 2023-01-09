package net.csibio.mslibrary.client.algorithm.compound;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Generator {

    @Autowired
    CompoundService compoundService;
    @Autowired
    SpectrumService spectrumService;
    @Autowired
    LibraryService libraryService;

    public Result generateBySmiles(String libraryId) {
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        if (spectrumDOS.size() == 0) {
            log.error("库中没有谱图");
            return new Result(false);
        }
        HashMap<String, List<SpectrumDO>> dataMap = new HashMap<>();
        dataMap = (HashMap<String, List<SpectrumDO>>) spectrumDOS.stream().collect(Collectors.groupingBy(SpectrumDO::getSmiles));
        List<CompoundDO> compoundDOS = new ArrayList<>();
        for (String smiles : dataMap.keySet()) {
            List<SpectrumDO> currentSpectrumDOS = dataMap.get(smiles);
            CompoundDO compoundDO = new CompoundDO();
            compoundDO.setSmiles(smiles);
            compoundDO.setLibraryId(libraryId);
            compoundDO.setCount(currentSpectrumDOS.size());
            compoundDOS.add(compoundDO);
        }
        compoundService.insert(compoundDOS, libraryId);
        log.info("生成化合物成功");
        return new Result(true);
    }

}
