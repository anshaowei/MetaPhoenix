package net.csibio.metaphoenix.client.domain.bean.chemical;

import lombok.Data;

import java.util.List;

@Data
public class Element {

    String name;

    String symbol;

    Integer atomicNumber;

    double monoWeight;

    double averageWeight;

    double maxAbundanceWeight;

    List<String> isotopes;
}
