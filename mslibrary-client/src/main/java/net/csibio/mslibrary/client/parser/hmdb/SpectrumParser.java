package net.csibio.mslibrary.client.parser.hmdb;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpectrumParser {

    @Autowired
    SpectrumService spectrumService;

    public void parse(String filePath) {
        log.info("开始执行数据库谱图解析");
    }

}
