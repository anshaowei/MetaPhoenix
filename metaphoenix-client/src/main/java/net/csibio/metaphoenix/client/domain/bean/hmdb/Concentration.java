package net.csibio.metaphoenix.client.domain.bean.hmdb;

import lombok.Data;

import java.util.List;

@Data
public class Concentration {

    String biospecimen;
    String value;
    String units;
    String subjectAge;
    String subjectSex;
    String subjectCondition;
    String patientAge;
    String patientSex;
    String patientInfo;
    String comment;
    List<Reference> references;
}
