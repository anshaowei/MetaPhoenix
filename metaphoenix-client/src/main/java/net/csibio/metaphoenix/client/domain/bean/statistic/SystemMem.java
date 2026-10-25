package net.csibio.metaphoenix.client.domain.bean.statistic;

import lombok.Data;

@Data
public class SystemMem {
    long jvmInitMem;
    long jvmMaxMem;
    long jvmUsedMem;
    long totalMem;
    long freeMem;
    long usedMem;
}
