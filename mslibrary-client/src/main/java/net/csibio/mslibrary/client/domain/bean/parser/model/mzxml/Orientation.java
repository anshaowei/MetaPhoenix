package net.csibio.mslibrary.client.domain.bean.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

@Data
public class Orientation {

    @XStreamAsAttribute
    protected String firstSpotID;

    @XStreamAsAttribute
    protected String secondSpotID;
}
