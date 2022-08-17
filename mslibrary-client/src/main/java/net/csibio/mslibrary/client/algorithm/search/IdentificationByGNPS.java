package net.csibio.mslibrary.client.algorithm.search;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.bean.spectrum.SimpleSpectrum;
import net.csibio.mslibrary.client.domain.db.CompoundDO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class IdentificationByGNPS {

    public List<CompoundDO> identifyCompounds(SimpleSpectrum simpleSpectrum) {
        return new ArrayList<>();
    }


}
