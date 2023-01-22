package net.csibio.mslibrary.client.domain.db;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.constants.enums.TraceOuterType;
import net.csibio.mslibrary.client.constants.enums.TraceStatus;
import net.csibio.mslibrary.client.constants.enums.TraceTemplate;
import net.csibio.mslibrary.client.domain.bean.trace.SubTrace;
import net.csibio.mslibrary.client.domain.bean.trace.TraceLog;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Slf4j
@Document(collection = "trace")
public class TraceDO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3238829837112156627L;

    @Id
    String id;

    String name;

    /**
     * @see TraceStatus
     */
    @Indexed
    String status;

    /**
     * @see TraceTemplate
     */
    @Indexed
    String template;

    /**
     * 关联ID
     */
    @Indexed
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
    Double progress = 0d;

    String features;

    Date createDate;

    Date lastModifiedDate;

    /**
     * 子任务
     */
    List<SubTrace> subTraces;

    public TraceDO() {
    }

    public TraceDO(TraceTemplate template, String outerId, String outerName, TraceOuterType outerType) {
        this.name = outerName + SymbolConst.DELIMITER + template.getName();
        this.outerId = outerId;
        this.outerName = outerName;
        this.outerType = outerType.getName();
        this.template = template.getName();
        this.status = TraceStatus.WAITING.getName();
    }

    public TraceDO addLog(String content) {
        if (logs == null) {
            logs = new ArrayList<>();
            logs.add(TraceLog.create("Task Start"));
        }
        TraceLog traceLog = TraceLog.create(content);
        logs.add(traceLog);
        return this;
    }

    public TraceDO addLog(List<String> contents) {
        if (logs == null) {
            logs = new ArrayList<>();
            logs.add(TraceLog.create("Task Start"));
        }
        for (String content : contents) {
            TraceLog traceLog = TraceLog.create(content);
            logs.add(traceLog);
        }
        return this;
    }

    public TraceDO addSubTrace(SubTrace subTrace) {
        if (subTraces == null) {
            subTraces = new ArrayList<>();
        }
        subTraces.add(subTrace);
        return this;
    }

    public TraceDO start() {
        if (logs == null || logs.size() == 0) {
            logs = new ArrayList<>();
            logs.add(TraceLog.create(name + " Task Start"));
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

    public TraceDO finish(String status) {
        addLog("Task End");
        this.status = status;
        totalCost = System.currentTimeMillis() - getStartTime();
        return this;
    }

    public TraceDO finish(TraceStatus status) {
        addLog("Task End");
        this.status = status.getName();
        totalCost = System.currentTimeMillis() - getStartTime();
        if (status.equals(TraceStatus.SUCCESS)){
            setProgress(100d);
        }
        return this;
    }

    public TraceDO finish(String status, String finishLog) {
        addLog(finishLog);
        return finish(status);
    }
}
