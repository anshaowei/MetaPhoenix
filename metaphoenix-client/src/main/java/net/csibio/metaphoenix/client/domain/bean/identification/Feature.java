package net.csibio.metaphoenix.client.domain.bean.identification;

import lombok.Data;
import net.csibio.aird.bean.common.Spectrum;
import net.csibio.metaphoenix.client.domain.bean.adduct.Adduct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Data
public class Feature {

    String id;

    String overviewId;

    List<String> dataIds = new ArrayList<>();

    List<String> runIds = new ArrayList<>();

    Double mz;

    Double rt;

    String adduct;

    Spectrum ms1Spectrum;

    Spectrum ms2Spectrum;

    HashSet<Adduct> adducts = new HashSet<>();

    /**
     * 鉴定信息，只有当进行了化合物库比对后才会填充
     */
    List<LibraryHit> libraryHits = new ArrayList<>();

}
