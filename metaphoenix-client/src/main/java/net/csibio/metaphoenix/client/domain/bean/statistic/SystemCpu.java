package net.csibio.metaphoenix.client.domain.bean.statistic;

import lombok.Data;

@Data
public class SystemCpu {
    int core;
    String info;
    long total;
    long user;
    long nice;
    long system;
    long idle;
    long iowait;
    long irq;
    long softirq;
    long steal;
}
