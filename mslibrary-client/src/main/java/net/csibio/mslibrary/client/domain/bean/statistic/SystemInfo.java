package net.csibio.mslibrary.client.domain.bean.statistic;

import lombok.Data;

@Data
public class SystemInfo {
    SystemCpu cpu;
    SystemEnv env;
    SystemMem mem;
    DiskInfo disk;
}
