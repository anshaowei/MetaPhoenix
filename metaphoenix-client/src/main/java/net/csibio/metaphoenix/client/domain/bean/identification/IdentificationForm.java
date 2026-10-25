package net.csibio.metaphoenix.client.domain.bean.identification;

import lombok.Data;

import java.util.List;

@Data
public class IdentificationForm {

    //待鉴定Features
    List<Feature> features;

    /**
     * 平台仪器参数相关配置平台仪器参数相关配置
     */
    String ionSource;

    String instrument;

    Float energy;

}
