package net.csibio.mslibrary.core.controller;

import net.csibio.mslibrary.client.algorithm.search.CommonSearch;
import net.csibio.mslibrary.client.algorithm.search.MetaProSearch;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.identification.IdentificationForm;
import net.csibio.mslibrary.client.domain.bean.params.IdentificationParams;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.query.LibraryQuery;
import net.csibio.mslibrary.client.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("identification")
public class IdentificationController {

    @Autowired
    MetaProSearch metaProSearch;
    @Autowired
    LibraryService libraryService;
    @Autowired
    CommonSearch commonSearch;

    @RequestMapping("identify")
    Result<IdentificationForm> identify(@RequestBody IdentificationForm identificationForm) {
        Result result = new Result<IdentificationForm>();
        //默认搜索全库
        List<LibraryDO> libraryDOList = libraryService.getAll(new LibraryQuery());
        IdentificationParams identificationParams = new IdentificationParams();
        List<String> libraryIds = new ArrayList<>();
        for (LibraryDO libraryDO : libraryDOList) {
            libraryIds.add(libraryDO.getId());
        }
        identificationParams.setLibraryIds(libraryIds);
        identificationParams.setMzTolerance(0.001);
        identificationParams.setTopN(10);
        identificationParams.setStrategy(1);
        result.setData(metaProSearch.identifyFeatures(identificationForm, identificationParams));
        return result;
    }

    @RequestMapping("commonIdentify")
    Result commonIdentify() {
        String filePath = "/Users/anshaowei/Downloads/(Centroid)_Met_08_Sirius.mgf";
        IdentificationParams identificationParams = new IdentificationParams();
        commonSearch.identify(filePath, identificationParams);
        return new Result(true);
    }


}
