package net.csibio.mslibrary.core.controller;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.enums.StatDim;
import net.csibio.mslibrary.client.constants.enums.StatType;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.statistic.*;
import net.csibio.mslibrary.client.domain.db.StatDO;
import net.csibio.mslibrary.client.domain.query.StatQuery;
import net.csibio.mslibrary.client.service.StatService;
import net.csibio.mslibrary.client.utils.DateUtil;
import net.csibio.mslibrary.core.components.SystemInfoMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("stat")
public class StatController {

    public static final String DATE_FORMAT = "yyyyMMdd";
    @Autowired
    StatService statService;
    @Autowired
    SystemInfoMonitor systemInfoMonitor;

    @RequestMapping(value = "/systemInfo")
    Result<SystemInfo> systemInfo() throws UnknownHostException, InterruptedException {
        SystemEnv env = systemInfoMonitor.env();
        SystemCpu cpu = systemInfoMonitor.cpu();
        SystemMem mem = systemInfoMonitor.mem();
        DiskInfo disk = systemInfoMonitor.disk();
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setCpu(cpu);
        systemInfo.setEnv(env);
        systemInfo.setMem(mem);
        systemInfo.setDisk(disk);
        return Result.OK(systemInfo);
    }

    @RequestMapping(value = "/doStatGlobalDaily")
    Result doStatGlobalDaily(@RequestParam(value = "date") String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date date = sdf.parse(dateStr);
        statService.globalStat(StatDim.Day, date);
        log.info("统计完成");
        return Result.OK();
    }

    @RequestMapping(value = "/getGlobalDailyStat")
    Result<StatDO> getGlobalDailyStat(@RequestParam(value = "date") String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date date = sdf.parse(dateStr);
        StatQuery query = new StatQuery();
        query.setDate(date);
        query.setDim(StatDim.Day.getDim());
        query.setType(StatType.Global_Total.getName());
        StatDO stat = statService.getOne(query, StatDO.class);
        if (stat == null) {
            stat = statService.globalStat(StatDim.Day, DateUtil.getTodayDate());
        }
        return Result.OK(stat);
    }
}
