package net.csibio.mslibrary.client.domain.bean.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

@Data
public class Plate {

    @XStreamAlias("plateManufacturer")
    OntologyEntry plateManufacturer;

    @XStreamAlias("plateModel")
    OntologyEntry plateModel;

    Pattern pattern;

    @XStreamImplicit(itemFieldName = "spot")
    List<Spot> spot;

    @XStreamAsAttribute
    Long plateID;

    @XStreamAsAttribute
    Long spotXCount;

    @XStreamAsAttribute
    Long spotYCount;

}
