package net.csibio.mslibrary.client.algorithm.decoy.generator;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("spectrumGenerator")
@Slf4j
public class SpectrumGenerator {

    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;

    public void spectrumBasedGenerate(String libraryId) {

        log.info("开始执行谱图生成伪肽段");
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        int count = 0;
        for (SpectrumDO spectrumDO : spectrumDOS) {
            count += spectrumDO.getMzs().length;
        }
        log.info("共计" + count + "个数据");
    }
}
