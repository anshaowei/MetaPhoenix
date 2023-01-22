package net.csibio.mslibrary.core.service.impl;

import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.constants.enums.TraceTemplate;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.TraceDO;
import net.csibio.mslibrary.client.domain.query.TraceQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.IDAO;
import net.csibio.mslibrary.client.service.TraceService;
import net.csibio.mslibrary.core.dao.TraceDAO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("traceService")
public class TraceServiceImpl implements TraceService {

    public final Logger logger = LoggerFactory.getLogger(TraceServiceImpl.class);

    @Autowired
    TraceDAO traceDAO;

    @Override
    public Result start(TraceDO trace, String newLog) {
        trace.start();
        logger.info("开始执行 " + trace.getName());
        if (StringUtils.isNotEmpty(newLog)) {
            trace.addLog(newLog);
        }
        trace.setProgress(0d);
        return update(trace);
    }

    @Override
    public Result start(TraceDO trace, int subTraceIdx, String newLog) {
        if (trace.getSubTraces() == null || subTraceIdx >= trace.getSubTraces().size()) {
            return Result.Error(ResultCode.SUBTRACE_NOT_EXISTED);
        }
        trace.getSubTraces().get(subTraceIdx).start();

        if (StringUtils.isNotEmpty(newLog)) {
            trace.getSubTraces().get(subTraceIdx).addLog(newLog);
            trace.getSubTraces().get(subTraceIdx).setProgress(0d);
        }
        return update(trace);
    }

    @Override
    public Result update(TraceDO trace, String newLog) {
        logger.info(newLog);
        trace.addLog(newLog);
        return update(trace);
    }

    @Override
    public Result update(TraceDO trace, int subTraceIdx, String newLog) {
        if (trace.getSubTraces() == null || subTraceIdx >= trace.getSubTraces().size()) {
            return Result.Error(ResultCode.SUBTRACE_NOT_EXISTED);
        }
        if (StringUtils.isNotEmpty(newLog)) {
            trace.getSubTraces().get(subTraceIdx).addLog(newLog);
        }
        return update(trace);
    }

    @Override
    public Result update(TraceDO trace, String newLog, Double progress) {
        if (newLog != null && !newLog.isEmpty()){
            logger.info(newLog);
            trace.addLog(newLog);
        }

        trace.setProgress(progress);
        return update(trace);
    }

    @Override
    public Result update(TraceDO trace, int subTraceIdx, String newLog, Double progress) {
        if (trace.getSubTraces() == null || subTraceIdx >= trace.getSubTraces().size()) {
            return Result.Error(ResultCode.SUBTRACE_NOT_EXISTED);
        }
        if (StringUtils.isNotEmpty(newLog)) {
            trace.getSubTraces().get(subTraceIdx).addLog(newLog);
            trace.getSubTraces().get(subTraceIdx).setProgress(progress);
        }
        return update(trace);
    }

    @Override
    public Result finish(TraceDO trace, String status, String newLog) {
        if (StringUtils.isNotEmpty(newLog)) {
            trace.addLog(newLog);
        }

        trace.finish(status);
        trace.setProgress(100d);
        logger.info("Task完整耗时测试：" + (trace.getTotalCost() > 1000 ? (trace.getTotalCost() / 1000 + "秒") : (trace.getTotalCost() + "毫秒")));
        return update(trace);
    }

    @Override
    public Result finish(TraceDO trace, int subTraceIdx, String status, String newLog) {
        if (trace.getSubTraces() == null || subTraceIdx >= trace.getSubTraces().size()) {
            return Result.Error(ResultCode.SUBTRACE_NOT_EXISTED);
        }
        if (StringUtils.isNotEmpty(newLog)) {
            trace.getSubTraces().get(subTraceIdx).addLog(newLog);
        }
        trace.getSubTraces().get(subTraceIdx).finish(status);
        trace.getSubTraces().get(subTraceIdx).setProgress(100d);
        return update(trace);
    }

    @Override
    public Result doTask(TraceTemplate traceTemplate) {
        return null;
    }

    @Override
    public IDAO<TraceDO, TraceQuery> getBaseDAO() {
        return traceDAO;
    }

    @Override
    public void beforeInsert(TraceDO trace) throws XException {
        trace.setCreateDate(new Date());
        trace.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeUpdate(TraceDO trace) throws XException {
        trace.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeRemove(String id) throws XException {

    }
}
