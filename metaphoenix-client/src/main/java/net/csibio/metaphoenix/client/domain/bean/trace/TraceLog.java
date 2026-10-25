package net.csibio.metaphoenix.client.domain.bean.trace;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

public record TraceLog(String content, Date time) implements Serializable {

    @Serial
    private static final long serialVersionUID = -3258829879112336617L;


    public static TraceLog create(String content) {
        return new TraceLog(content, new Date());
    }
}
