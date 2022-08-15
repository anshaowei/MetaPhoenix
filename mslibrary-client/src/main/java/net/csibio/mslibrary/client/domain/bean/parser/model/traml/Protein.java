package net.csibio.mslibrary.client.domain.bean.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * Protein for which one or more transitions are intended to identify
 */
@Data
@XStreamAlias("Protein")
public class Protein {

    @XStreamImplicit(itemFieldName = "cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName = "userParam")
    List<UserParam> userParams;

    /**
     * Amino acid sequence of the protein
     */
    @XStreamAlias("Sequence")
    String sequence;

    /**
     * Identifier for the protein to be used for referencing within a document
     * required
     */
    @XStreamAsAttribute
    String id;

}