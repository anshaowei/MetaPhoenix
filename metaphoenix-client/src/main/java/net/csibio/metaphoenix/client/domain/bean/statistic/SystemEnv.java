package net.csibio.metaphoenix.client.domain.bean.statistic;

import lombok.Data;

@Data
public class SystemEnv {
    String userName;
    String computerName;
    String userDomain;
    String osName;
    String osVersion;
    String osArch;
    String localIp;
    String hostName;
    String javaVersion;
    String javaVendor;
    String timezone;
}
