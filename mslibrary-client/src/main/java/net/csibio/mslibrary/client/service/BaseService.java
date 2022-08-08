package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.query.PageQuery;
import net.csibio.mslibrary.client.exceptions.XException;

import java.util.HashMap;
import java.util.List;

public interface BaseService<T, Q extends PageQuery> {

    default T tryGetById(String id, ResultCode code) throws XException {
        T t;
        try {
            t = getBaseDAO().getById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (t == null) {
            throw new XException(code);
        }
        return t;
    }

    default <K> K tryGetById(String id, Class<K> clazz, ResultCode code) throws XException {
        K k;
        try {
            k = getBaseDAO().getById(id, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (k == null) {
            throw new XException(code);
        }
        return k;
    }

    default T getById(String id) {
        try {
            return getBaseDAO().getById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    default <K> K getById(String id, Class<K> clazz) {
        try {
            return getBaseDAO().getById(id, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    default boolean exists(Q q) {
        try {
            return getBaseDAO().exists(q);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    default Result remove(String id) {
        if (id == null || id.isEmpty()) {
            return Result.Error(ResultCode.ID_CANNOT_BE_NULL_OR_ZERO);
        }
        try {
            beforeRemove(id);
            getBaseDAO().remove(id);
            afterRemove(id);
            return Result.OK();
        } catch (Exception e) {
            return Result.Error(ResultCode.DELETE_ERROR);
        }
    }

    default Result remove(Q q) {
        try {
            getBaseDAO().remove(q);
            return Result.OK();
        } catch (Exception e) {
            return Result.Error(ResultCode.DELETE_ERROR);
        }
    }

    default Result<T> insert(T t) {
        try {
            beforeInsert(t);
            getBaseDAO().insert(t);
            afterInsert(t);
            return Result.OK(t);
        } catch (XException xe) {
            return Result.Error(xe.getCode(), xe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.Error(ResultCode.INSERT_ERROR);
        }
    }

    default Result<List<T>> insert(List<T> tList) {
        try {
            for (T t : tList) {
                beforeInsert(t);
            }
            List<T> successInsertList = getBaseDAO().insert(tList);
            for (T t : tList) {
                afterInsert(t);
            }
            return Result.OK(successInsertList);
        } catch (XException xe) {
            return Result.Error(xe.getCode(), xe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.Error(ResultCode.INSERT_ERROR);
        }
    }

    default Result<T> update(T t) {
        try {
            beforeUpdate(t);
            getBaseDAO().update(t);
            afterUpdate(t);
            return Result.OK(t);
        } catch (XException xe) {
            return Result.Error(xe.getCode(), xe.getMessage());
        } catch (Exception e) {
            return Result.Error(ResultCode.UPDATE_ERROR);
        }
    }

    default Result updateFirst(HashMap<String, Object> queryMap, HashMap<String, Object> fieldMap) {
        boolean res = getBaseDAO().updateFirst(queryMap, fieldMap);
        if (res) {
            return Result.OK();
        } else {
            return Result.Error(ResultCode.UPDATE_ERROR);
        }
    }

    default Result updateAll(HashMap<String, Object> queryMap, HashMap<String, Object> fieldMap) {
        boolean res = getBaseDAO().updateAll(queryMap, fieldMap);
        if (res) {
            return Result.OK();
        } else {
            return Result.Error(ResultCode.UPDATE_ERROR);
        }
    }

    default <K> K getOne(Q q, Class<K> clazz) {
        return getBaseDAO().getOne(q, clazz);
    }

    default Result<List<T>> getList(Q q) {
        long start = System.currentTimeMillis();
        List<T> tList = getBaseDAO().getList(q);
        if (System.currentTimeMillis() - start > 500) {
            System.out.println("列表请求时间过长:" + (System.currentTimeMillis() - start) + "ms");
        }
        long totalCount = 0;
        if (q.getEstimateCount()) {
            totalCount = getBaseDAO().estimatedCount();
        } else {
            totalCount = getBaseDAO().count(q);
        }

        Result<List<T>> result = new Result<>(true);
        result.setData(tList);
        result.setTotal(totalCount);
        result.setPageSize(q.getPageSize());
        return result;
    }

    default <K> Result<List<K>> getList(Q q, Class<K> clazz) {
        long start = System.currentTimeMillis();
        List<K> tList = getBaseDAO().getList(q, clazz);
        if (System.currentTimeMillis() - start > 500) {
            System.out.println("列表请求时间过长:" + (System.currentTimeMillis() - start) + "ms");
        }
        long totalCount = 0;
        if (q.getEstimateCount()) {
            totalCount = getBaseDAO().estimatedCount();
        } else {
            totalCount = getBaseDAO().count(q);
        }
        Result<List<K>> result = new Result<>(true);
        result.setData(tList);
        result.setTotal(totalCount);
        result.setPageSize(q.getPageSize());
        return result;
    }

    default List<T> getAll(Q q) {
        return getBaseDAO().getAll(q);
    }

    default <K> List<K> getAll(Q q, Class<K> clazz) {
        return getBaseDAO().getAll(q, clazz);
    }

    default long count(Q q) {
        return getBaseDAO().count(q);
    }

    default long estimatedCount() {
        return getBaseDAO().estimatedCount();
    }

    IDAO<T, Q> getBaseDAO();

    default void beforeInsert(T t) throws XException {
    }

    default void afterInsert(T t) throws XException {
    }

    default void beforeUpdate(T t) throws XException {
    }

    default void afterUpdate(T t) throws XException {
    }

    default void beforeRemove(String id) throws XException {
    }


    default void afterRemove(String id) throws XException {
    }

}
