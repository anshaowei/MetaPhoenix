package net.csibio.metaphoenix.client.domain.bean.trace;

import lombok.Data;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.metaphoenix.client.constants.enums.TraceOuterType;
import net.csibio.metaphoenix.client.constants.enums.TraceStatus;
import net.csibio.metaphoenix.client.constants.enums.TraceTemplate;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class SubTrace implements Serializable {

    @Serial
    private static final long serialVersionUID = -3298829833718215127L;

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

    public SubTrace() {
    }

    public SubTrace(TraceTemplate template, String outerId, String outerName, TraceOuterType outerType) {
        this.name = outerName + SymbolConst.DELIMITER + template.getName();
        this.outerId = outerId;
        this.outerName = outerName;
        this.outerType = outerType.getName();
        this.template = template.getName();
        this.status = TraceStatus.WAITING.getName();
    }

    public SubTrace addLog(String content) {
        if (logs == null) {
            logs = new ArrayList<>();
            logs.add(TraceLog.create("任务开始"));
        }
        TraceLog traceLog = TraceLog.create(content);

        logs.add(traceLog);

        return this;
    }

    public SubTrace addLog(List<String> contents) {
        if (logs == null) {
            logs = new ArrayList<>();
            logs.add(TraceLog.create("任务开始"));
        }
        for (String content : contents) {
            TraceLog traceLog = TraceLog.create(content);
            logs.add(traceLog);
        }

        return this;
    }

    public SubTrace start() {
        if (logs == null || logs.size() == 0) {
            logs = new ArrayList<>();
            logs.add(TraceLog.create("任务开始"));
        }
        status = TraceStatus.RUNNING.getName();

        return this;
    }

    public Long getStartTime() {
        if (logs == null || logs.size() == 0) {
            return null;
        }
        TraceLog traceLog = logs.get(0);
        return traceLog.time().getTime();
    }

    public SubTrace finish(String status) {
        addLog("任务结束");
        this.status = status;
        totalCost = System.currentTimeMillis() - getStartTime();

        return this;
    }

    public SubTrace finish(TraceStatus status) {
        addLog("任务结束");
        this.status = status.getName();
        totalCost = System.currentTimeMillis() - getStartTime();

        return this;
    }

    public SubTrace finish(String status, String finishLog) {
        addLog(finishLog);
        addLog("任务结束");
        this.status = status;
        totalCost = System.currentTimeMillis() - getStartTime();

        return this;
    }
}
