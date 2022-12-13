package net.csibio.mslibrary.client.algorithm.decoy.generator;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component("spectrumGenerator")
@Slf4j
public class SpectrumGenerator {

    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;

    public void spectrumBasedGenerate(String libraryId) {
        LibraryDO libraryDO = libraryService.getById(libraryId);
        LibraryDO decoyLibraryDO = new LibraryDO();
        decoyLibraryDO.setName(libraryDO.getName() + "_decoy");
        if (libraryService.insert(decoyLibraryDO).isFailed()) {
            log.error("插入同名Decoy库失败");
            return;
        }

        log.info("开始执行谱图生成伪肽段");
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        HashMap<String, List<SpectrumDO>> smilesMap = new HashMap<>();
        for (SpectrumDO spectrumDO : spectrumDOS) {
            if (smilesMap.containsKey(spectrumDO.getSmiles())) {
                smilesMap.get(spectrumDO.getSmiles()).add(spectrumDO);
            } else {
                List<SpectrumDO> list = new ArrayList<>();
                list.add(spectrumDO);
                smilesMap.put(spectrumDO.getSmiles(), list);
            }
        }

        //test spectrumDO in smilesMap whether precursorMz is the same
        int count = 0;
        for (String smiles : smilesMap.keySet()) {
            List<SpectrumDO> spectrumDOList = smilesMap.get(smiles);
            if (spectrumDOList.get(0).getPrecursorMz() != null) {
                double precursorMz = spectrumDOList.get(0).getPrecursorMz();
                for (SpectrumDO spectrumDO : spectrumDOList) {
                    if (spectrumDO.getPrecursorMz() != precursorMz) {
                        log.error(smiles + "谱图生成伪肽段时发现同一Smiles下的谱图的PrecursorMz不同");
                        count++;
                    }
                }
            }
        }

        log.info("finish test spectrumDO in smilesMap whether precursorMz is the same {}", count);

    }
}
