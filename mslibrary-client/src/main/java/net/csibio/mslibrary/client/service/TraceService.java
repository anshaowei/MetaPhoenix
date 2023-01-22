package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.constants.enums.TraceTemplate;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.TraceDO;
import net.csibio.mslibrary.client.domain.query.TraceQuery;

public interface TraceService extends BaseService<TraceDO, TraceQuery> {

    Result start(TraceDO trace, String newLog);

    Result start(TraceDO trace, int subTraceIdx, String newLog);

    Result update(TraceDO trace, String newLog);

    Result update(TraceDO trace, int subTraceIdx, String newLog);

    Result update(TraceDO trace, String newLog, Double progress);

    Result update(TraceDO trace, int subTraceIdx, String newLog, Double progress);

    Result finish(TraceDO trace, String status, String newLog);

    Result finish(TraceDO trace, int subTraceIdx, String status, String newLog);

    Result doTask(TraceTemplate traceTemplate);

}
