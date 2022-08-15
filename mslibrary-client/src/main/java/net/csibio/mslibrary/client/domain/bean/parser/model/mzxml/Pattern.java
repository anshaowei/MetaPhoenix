package net.csibio.mslibrary.client.domain.bean.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
public class Pattern {

    @XStreamAlias("spottingPattern")
    OntologyEntry spottingPattern;


    Orientation orientation;
}
