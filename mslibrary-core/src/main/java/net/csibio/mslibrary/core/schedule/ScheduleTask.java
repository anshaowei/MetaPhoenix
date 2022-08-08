package net.csibio.mslibrary.core.schedule;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.enums.StatDim;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.StatService;
import net.csibio.mslibrary.client.utils.DateUtil;
import net.csibio.mslibrary.core.config.VMProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component
@Slf4j
public class ScheduleTask {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    VMProperties vmProperties;
    @Autowired
    LibraryService libraryService;
    @Autowired
    StatService statService;

    //定时扫描Redis中的构建项目通道
    @Scheduled(cron = "0 0 1 * * ?") //每天凌晨一点统计前一天的数据
    public void dailyGlobalStat() throws ParseException {
        statService.globalStat(StatDim.Day, DateUtil.getYesterdayDate());
    }
}
