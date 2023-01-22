package net.csibio.mslibrary.core.controller;

import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.trace.TraceRow;
import net.csibio.mslibrary.client.domain.db.TraceDO;
import net.csibio.mslibrary.client.domain.query.TraceQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.BaseService;
import net.csibio.mslibrary.client.service.TraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("trace")
public class TraceController extends BaseController<TraceDO, TraceQuery> {

    @Autowired
    TraceService traceService;

    @RequestMapping("/list")
    Result<List<TraceRow>> list(TraceQuery query) {
        query.setSortColumn("createDate");
        query.setOrderBy(Sort.Direction.DESC);
        Result<List<TraceRow>> taskList = traceService.getList(query, TraceRow.class);
        return taskList;
    }

    @RequestMapping(value = "/detail")
    Result<TraceDO> detail(@RequestParam(value = "id", required = true) String id) throws XException {
        Result result = new Result(true);
        TraceDO trace = traceService.tryGetById(id, ResultCode.TRACE_NOT_EXISTED);
        result.setData(trace);
        return result;
    }

    @Override
    BaseService<TraceDO, TraceQuery> getBaseService() {
        return traceService;
    }
}
