package net.csibio.mslibrary.core.controller;

import net.csibio.mslibrary.client.algorithm.search.Identification;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.identification.Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("identification")
public class IdentificationController {

    @Autowired
    Identification identification;

    @RequestMapping("identify")
    Result identify(@RequestBody Feature feature) {
        Result result = new Result();
        result.setData(identification.identifyFeature(feature, new ArrayList<>()));
        return result;
    }

}
