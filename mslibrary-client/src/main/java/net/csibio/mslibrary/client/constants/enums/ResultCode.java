package net.csibio.mslibrary.client.constants.enums;

import java.io.Serializable;

public enum ResultCode implements Serializable {

    COMPOUND_NOT_EXISTED("COMPOUND_NOT_EXISTED", "该化合物不存在"),
    DATE_TIME_FORMAT_ERROR("DATE_TIME_FORMAT_ERROR", "时间格式错误,请按照yyyyMMdd-HHmmss的格式传入数据"),
    DELETE_ERROR("DELETE_ERROR", "删除数据失败"),
    DUPLICATED_COMPOUND_EXIST("DUPLICATED_COMPOUND_EXIST", "有重复存在的化合物"),
    EXCEPTION("SYSTEM_EXCEPTION", "系统繁忙,稍后再试!"),
    ID_CANNOT_BE_NULL_OR_ZERO("ID_CANNOT_BE_NULL_OR_ZERO", "ID不能为空或者0"),
    INSERT_ERROR("INSERT_ERROR", "插入数据失败"),
    LIBRARY_NOT_EXISTED("LIBRARY_NOT_EXISTED", "指定的库不存在"),
    METHOD_NOT_EXISTED("METHOD_NOT_EXISTED", "参数方案不存在"),
    SPECTRA_NOT_EXISTED("SPECTRA_NOT_EXISTED", "光谱数据为空"),
    SPECTRUM_NOT_EXISTED("SPECTRUM_NOT_EXISTED", "对应的原始谱图不存在"),
    SUBTRACE_NOT_EXISTED("SUBTRACE_NOT_EXISTED", "子日志不存在"),
    TRACE_NOT_EXISTED("TRACE_NOT_EXISTED", "跟踪日志不存在"),
    UPDATE_ERROR("UPDATE_ERROR", "更新数据失败"),
    ;

    private static final long serialVersionUID = -799302222165012777L;
    private String code = "";
    private String message = "";

    /**
     * @param code
     * @param message
     */
    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message + "(" + code + ")";
    }

}
