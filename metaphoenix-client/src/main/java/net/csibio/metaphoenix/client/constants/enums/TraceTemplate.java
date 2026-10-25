package net.csibio.metaphoenix.client.constants.enums;

public enum TraceTemplate {

    SCAN_AND_UPDATE_RUNS("SCAN_AND_UPLOAD", "更新进样"),
    SCAN_RUNS("SCAN_RUNS", "扫描进样"),
    UPLOAD_RUNS("UPLOAD_RUNS", "上传进样"),
    UNTARGET_ANALYZE("UNTARGET_ANALYZE", "非靶分析"),
    LIBRARY_PUSH("LIBRARY_PUSH", "推送库"),
    TARGET_ANALYZE("TARGET_ANALYZE", "靶向分析"),
    PRE_QC("PRE_QC", "预质控"),
    TARGET_EXTRACTION("TARGET_EXTRACTION", "靶向提取"),
    RI_CALIBRATION("RI_CALIBRATION", "RT校准"),
    BUILD_LIBRARY("BUILD_LIBRARY_TARGET", "靶向建库"),
    ;

    private final String name;

    private final String desc;

    TraceTemplate(String templateName, String desc) {
        this.name = templateName;
        this.desc = desc;
    }

    public static TraceTemplate getByName(String name) {
        for (TraceTemplate template : values()) {
            if (template.getName().equals(name)) {
                return template;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
