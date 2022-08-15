package net.csibio.mslibrary.core.parser.hmdb;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.LibraryConst;
import net.csibio.mslibrary.client.constants.enums.LibraryType;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.query.CompoundQuery;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

@Slf4j
@Component
public class HmdbParser {

    @Autowired
    CompoundService compoundService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;
    @Autowired
    HmdbParseTask hmdbParseTask;

    //每批次处理的化合物数目
    private static int BATCH_SIZE = 10000;

    public Result parse(String filePath) {
        long totalStart = System.currentTimeMillis();
        String libraryId = LibraryConst.HMDB_COMPOUND;
        LibraryDO library = libraryService.getById(libraryId);
        if (library == null) {
            library = new LibraryDO();
            library.setType(LibraryType.Metabolomics.getName());
            library.setName(libraryId);
            libraryService.insert(library);
            log.info("HMDB镜像库不存在,已创建新的HMDB库");
        }
        compoundService.remove(new CompoundQuery(), libraryId);
        log.info("已经删除HMDB旧数据,开始解析新的源文件");

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String headerLine = reader.readLine();
            while (!headerLine.equals("<metabolite>")) {
                headerLine = reader.readLine();
            }
            StringBuilder metaSingleBuilder = new StringBuilder("<root>");
            metaSingleBuilder.append(headerLine);
            boolean startAcquisition = true;
            boolean startParse = false;
            int total = 0;
            int batchCount = 1;
            long start = System.currentTimeMillis();
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.equals("<metabolite>")) {
                    batchCount++;
                    startAcquisition = true;
                }
                if (line.equals("</metabolite>")) {
                    startAcquisition = false;
                    if (batchCount == BATCH_SIZE) {
                        total += BATCH_SIZE;
                        startParse = true;
                    }
                    metaSingleBuilder.append(line);
                }
                if (startAcquisition) {
                    metaSingleBuilder.append(line);
                } else {
                    if (startParse) {
                        metaSingleBuilder.append("</root>");
                        log.info("开始插入第" + total + "条化合物");
                        hmdbParseTask.parse(new ByteArrayInputStream(metaSingleBuilder.toString().getBytes()), libraryId);
                        log.info("第" + total + "条化合物插入成功,耗时:" + (System.currentTimeMillis() - start) / 1000 + "秒");
                        start = System.currentTimeMillis();
                        metaSingleBuilder = new StringBuilder("<root>");
                        batchCount = 0;
                        startParse = false;
                    }
                }
            }
            if (batchCount > 0) {
                metaSingleBuilder.append("</root>");
                total += batchCount;
                hmdbParseTask.parse(new ByteArrayInputStream(metaSingleBuilder.toString().getBytes()), libraryId);
            }

            library.setCount(total);
            libraryService.update(library);
            log.info(total + "条新化合物插入完毕,耗时:" + (System.currentTimeMillis() - totalStart) / 1000 + "秒");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(true);
    }
}
