package net.csibio.metaphoenix.client.domain.bean.spectrum;

import lombok.Data;

import java.util.Date;

@Data
public class AnnotationHistory {

    String compoundName;

    String ionSource;

    String compoundSource;

    String instrument;

    String pi;

    String dataCollector;

    String adduct;

    String scan;

    Double precursorMz;

    Double exactMass;

    Integer charge;

    String casNumber;

    String pubmedId;

    String smiles;

    String inchi;

    String inchiAUX;

    Integer libraryClass;

    String spectrumId;

    String ionMode;

    Date createTime;

    String taskId;

    String userId;

}
