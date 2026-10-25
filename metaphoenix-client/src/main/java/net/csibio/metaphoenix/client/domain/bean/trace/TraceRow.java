package net.csibio.metaphoenix.client.domain.bean.trace;

import lombok.Data;
import net.csibio.metaphoenix.client.constants.enums.TraceStatus;
import net.csibio.metaphoenix.client.constants.enums.TraceTemplate;

import java.util.Date;
import java.util.List;

@Data
public class TraceRow {

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
     * 关联类型, 项目,库
     */
    String outerType;

    List<TraceLog> logs;

    Long totalCost;

    /**
     * 任务运行参数
     */
    String params;

    /**
     * 任务执行类
     */
    String clazz;

    /**
     * 任务执行方法
     */
    String method;

    /**
     * 执行进度(按照百分比)
     */
    Double progress;

    String features;

    Date createDate;

    Date lastModifiedDate;
}
