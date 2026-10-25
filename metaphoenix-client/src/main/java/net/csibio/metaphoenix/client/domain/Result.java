package net.csibio.metaphoenix.client.domain;

import net.csibio.metaphoenix.client.constants.enums.ResultCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1738821497566027418L;
    public Pagination pagination = new Pagination();
    /**
     * 是否执行成功
     */
    private boolean success = false;
    /**
     * 错误码
     */
    private String msgCode;
    /**
     * 错误提示信息
     */
    private String msgInfo;
    /**
     * Http 返回状态
     */
    private int status;
    /**
     * 错误信息列表
     */
    private List<String> errorList;
    /**
     * 单值返回,泛型
     */
    private T data;
    /**
     * 备用存储字段,用于扩展多个返回类型的情况
     */
    private HashMap featureMap;

    public Result() {
    }

    public Result(boolean success) {
        this.success = success;
    }

    public static Result OK() {
        Result r = new Result();
        r.setSuccess(true);
        return r;
    }

    public static <T> Result<T> OK(T model) {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setData(model);
        return r;
    }

    public static Result Error(String msg) {
        Result result = new Result();
        result.setSuccess(false);
        result.setMsgCode(ResultCode.EXCEPTION.getCode());
        result.setMsgInfo(msg);
        return result;
    }

    public static Result Error(String code, String msg) {
        Result result = new Result();
        result.setSuccess(false);
        result.setMsgCode(code);
        result.setMsgInfo(msg);
        return result;
    }

    public static Result Error(ResultCode resultCode) {
        Result result = new Result();
        result.setErrorResult(resultCode);
        return result;
    }

    public static Result Error(ResultCode resultCode, int status) {
        Result result = new Result();
        result.setErrorResult(resultCode);
        result.setStatus(status);
        return result;
    }

    public static Result build(Object object) {
        Result result = new Result(true);
        result.setData(object);
        return result;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public long getTotal() {
        return pagination.getTotal();
    }

    public void setTotal(long total) {
        pagination.setTotal(total);
    }

    public int getPageSize() {
        return pagination.getPageSize();
    }

    public void setPageSize(int pageSize) {
        pagination.setPageSize(pageSize);
    }

    public long getPage() {
        return pagination.getCurrent();
    }

    public void setPage(long currentPageNo) {
        pagination.setCurrent(currentPageNo);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isFailed() {
        return !success;
    }

    public String getMsgCode() {
        return msgCode;
    }

    public void setMsgCode(String msgCode) {
        this.msgCode = msgCode;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T value) {
        this.data = value;
        return this;
    }

    public HashMap getFeatureMap() {
        if (featureMap == null) {
            featureMap = new HashMap();
        }
        return featureMap;
    }

    public void setFeatureMap(HashMap featureMap) {
        this.featureMap = featureMap;
    }

    public void put(String key, Object value) {
        getFeatureMap().put(key, value);
    }

    public Object get(String key) {
        return getFeatureMap().get(key);
    }

    public String getMsgInfo() {
        return msgInfo;
    }

    public void setMsgInfo(String msgInfo) {
        this.msgInfo = msgInfo;
    }

    public Result setResultCode(ResultCode code) {
        this.msgCode = code.getCode();
        this.msgInfo = code.getMessage();
        return this;
    }

    public Result setErrorResult(ResultCode resultCode) {
        this.success = false;
        this.msgCode = resultCode.getCode();
        this.msgInfo = resultCode.getMessage();
        return this;
    }

    public Result setErrorResult(String code, String msg) {
        this.success = false;
        this.msgCode = code;
        this.msgInfo = msg;
        return this;
    }

    public Result setErrorResult(ResultCode resultCode, String msg) {
        return this.setErrorResult(resultCode.getCode(), msg);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTotalPage() {
        if (getPageSize() > 0 && getTotal() > 0) {
            return (getTotal() % getPageSize() == 0L ? (getTotal() / getPageSize()) : (getTotal() / getPageSize() + 1));
        } else {
            return 0;
        }
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public Result setErrorList(List<String> errorList) {
        this.errorList = errorList;
        return this;
    }

    public void addErrorMsg(String errorMsg) {
        if (errorList == null) {
            errorList = new ArrayList<>();
        }
        errorList.add(errorMsg);
    }

}
