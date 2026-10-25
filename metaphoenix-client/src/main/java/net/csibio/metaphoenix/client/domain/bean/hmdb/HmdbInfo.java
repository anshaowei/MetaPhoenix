package net.csibio.metaphoenix.client.domain.bean.hmdb;

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
//    Biological biological; 已经迁移至CompoundDO
    List<Concentration> normalConcentrations;
    List<Concentration> abnormalConcentrations;
    List<Disease> diseases;
    List<Reference> references;
    List<ProteinAssociation> proteinAssociations;
}
