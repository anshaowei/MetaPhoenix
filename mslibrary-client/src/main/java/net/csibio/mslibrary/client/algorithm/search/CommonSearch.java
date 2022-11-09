package net.csibio.mslibrary.client.algorithm.search;

import io.github.msdk.io.mgf.MgfFileImportMethod;
import io.github.msdk.io.mgf.MgfMsSpectrum;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.bean.params.IdentificationParams;
import net.csibio.mslibrary.client.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CommonSearch {

    @Autowired
    LibraryService libraryService;

    public void identify(String filePath, IdentificationParams identificationParams) {
        //1.文件解析
        List<MgfMsSpectrum> mgfMsSpectrumList = new ArrayList<>();
        try {
            File file = new File(filePath);
            MgfFileImportMethod mgfFileImportMethod = new MgfFileImportMethod(file);
            mgfFileImportMethod.execute();
            mgfMsSpectrumList = mgfFileImportMethod.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //2.搜索本地数据库
        List<String> libraryIds = identificationParams.getLibraryIds();


        //3.输出标准结果

    }
}
