package net.csibio.mslibrary.client.algorithm.search;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.bean.identification.Feature;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Identification {

    public Feature identifyFeature(Feature feature) {

        return feature;
    }

}
