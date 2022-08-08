package net.csibio.mslibrary.client.domain.bean.statistic;

import lombok.Data;

@Data
public class DiskInfo {

    long total;
    long available;
    long free;

    String path;
}
