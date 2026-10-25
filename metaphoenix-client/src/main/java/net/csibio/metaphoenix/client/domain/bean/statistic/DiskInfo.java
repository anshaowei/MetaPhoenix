package net.csibio.metaphoenix.client.domain.bean.statistic;

import lombok.Data;

@Data
public class DiskInfo {

    long total;
    long available;
    long free;

    String path;
}
