package net.csibio.mslibrary.client.domain.bean.hmdb;

import lombok.Data;

import java.util.List;

@Data
public class HmdbInfo {

    /**
     * 综合参考
     */
    String synthesisReference;
    Taxonomy taxonomy;
    List<Descendant> ontology;
    List<Property> experimentalProperties;
    List<Property> predictedProperties;
    List<SpectrumLink> spectra;
    Biological biological;
    List<Concentration> concentrations;
    List<Disease> diseases;
    List<Reference> references;
    List<ProteinAssociation> proteinAssociations;
}
