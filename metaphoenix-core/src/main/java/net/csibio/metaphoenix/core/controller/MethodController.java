package net.csibio.metaphoenix.core.controller;

import net.csibio.aird.bean.common.IdName;
import net.csibio.metaphoenix.client.constants.enums.ResultCode;
import net.csibio.metaphoenix.client.domain.Result;
import net.csibio.metaphoenix.client.domain.bean.common.LabelValue;
import net.csibio.metaphoenix.client.domain.db.MethodDO;
import net.csibio.metaphoenix.client.domain.query.MethodQuery;
import net.csibio.metaphoenix.client.exceptions.XException;
import net.csibio.metaphoenix.client.service.BaseService;
import net.csibio.metaphoenix.client.service.MethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("method")
@RestController
public class MethodController extends BaseController<MethodDO, MethodQuery> {

    @Autowired
    MethodService methodService;


    /**
     * 获取MethodDO的列表
     *
     * @param pageSize    每一页要显示的个数
     * @param currentPage 当前页面是第几页, 从1开始
     * @return ListVO对象, 含有请求的list数据和总页数信息
     */
    @RequestMapping("/list")
    Result list(@RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize,
                @RequestParam(value = "currentPage", required = false, defaultValue = "1") int currentPage,
                @RequestParam(value = "name", required = false) String name) {
        MethodQuery query = new MethodQuery();
        if (name != null) {
            query.setName(name);
        }
        query.setPageSize(pageSize);
        query.setCurrent(currentPage);
        Result result = methodService.getList(query);
        return result;
    }

    /**
     * 根据给定的methodId, 返回MethodDO的信息
     *
     * @param methodId MethodDO的数据库id
     * @return MethodDO对象
     */
    @RequestMapping("/detail")
    MethodDO detail(@RequestParam(value = "methodId", required = true) String methodId) throws XException {
        return methodService.tryGetById(methodId, ResultCode.METHOD_NOT_EXISTED);
    }

    @RequestMapping("/fetchMethodLabelValues")
    Result<List<LabelValue>> fetchMethodLabelValues() {
        MethodQuery query = new MethodQuery();
        List<IdName> libraryList = methodService.getAll(query, IdName.class);
        List<LabelValue> lvList = new ArrayList<>();
        for (IdName idName : libraryList) {
            lvList.add(new LabelValue(idName.name(), idName.id()));
        }
        Result<List<LabelValue>> result = new Result(true);
        result.setData(lvList);
        return result;
    }

    @Override
    BaseService<MethodDO, MethodQuery> getBaseService() {
        return methodService;
    }
}
