package net.csibio.mslibrary.core.controller;

import net.csibio.mslibrary.client.algorithm.search.Identification;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.identification.Feature;
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
    Identification identification;
    @Autowired
    LibraryService libraryService;

    @RequestMapping("identify")
    Result identify(@RequestBody Feature feature) {
        Result result = new Result();
        //默认搜索全库
        List<LibraryDO> libraryDOList = libraryService.getAll(new LibraryQuery());
        IdentificationParams identificationParams = new IdentificationParams();
        List<String> libraryIds = new ArrayList<>();
        for (LibraryDO libraryDO : libraryDOList) {
            libraryIds.add(libraryDO.getId());
        }
        identificationParams.setLibraryIds(libraryIds);
        identificationParams.setMzTolerance(0.001);
        result.setData(identification.identifyFeatureBySpectrum(feature, identificationParams));
        return result;
    }

}
