package net.csibio.mslibrary.core.controller;

import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.identification.Feature;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("identification")
public class IdentificationController {

    @RequestMapping("identify")
    Result identify(@RequestBody List<Feature> features) {
        int a = 0;
        features.forEach(feature -> feature.setArea(0.0));
        Result result = new Result();
        result.setData(features);
        return result;
    }

}
