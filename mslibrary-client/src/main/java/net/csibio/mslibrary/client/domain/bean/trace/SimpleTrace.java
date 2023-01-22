package net.csibio.mslibrary.client.domain.bean.trace;

import lombok.Data;
import net.csibio.mslibrary.client.constants.enums.TraceStatus;
import net.csibio.mslibrary.client.constants.enums.TraceTemplate;

import java.util.Date;

@Data
public class SimpleTrace {

    String id;

    String name;

    /**
     * @see TraceStatus
     */
    String status;

    /**
     * @see TraceTemplate
     */
    String template;

    /**
     * 关联ID
     */
    String outerId;

    /**
     * 关联名称
     */
    String outerName;

    /**
     * 进度
     */
    Double progress;

    /**
     * 关联类型, 项目,库
     */
    String outerType;

    Date createDate;
}
