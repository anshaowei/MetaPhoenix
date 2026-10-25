package net.csibio.metaphoenix.client.service;

import net.csibio.aird.bean.common.IdName;
import net.csibio.aird.bean.common.IdNameType;
import net.csibio.metaphoenix.client.domain.query.PageQuery;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.List;

public interface IDAO<T, Q extends PageQuery> {

    T getById(String id);

    <K> K getById(String id, Class<K> clazz);

    T getOne(Q query);

    <K> K getOne(Q query, Class<K> clazz);

    boolean exists(Q query);

    @SuppressWarnings("Carefully Using!!!")
    List<T> getAll(Q query);

    <K> List<K> getAll(Q query, Class<K> clazz);

    List<T> getList(Q query);

    <K> List<K> getList(Q query, Class<K> clazz);

    long count(Q query);

    long estimatedCount();

    T insert(T t);

    List<T> insert(List<T> list);

    T update(T t);

    List<T> update(List<T> list);

    boolean updateFirst(HashMap<String, Object> queryMap, HashMap<String, Object> fieldMap);

    boolean updateAll(HashMap<String, Object> queryMap, HashMap<String, Object> fieldMap);

    void remove(String id);

    void remove(Q query);

    List<IdName> getIdNameList(Q query);

    List<IdNameType> getIdNameTypeList(Q query);

    Query buildQuery(Q targetQuery);
}
