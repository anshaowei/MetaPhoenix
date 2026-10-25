package net.csibio.metaphoenix.client.service;

import net.csibio.metaphoenix.client.domain.query.PageQuery;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.List;

public interface IMultiDAO<T, Q extends PageQuery> {

    T getById(String id, String routerId);

    <K> K getById(String id, Class<K> clazz, String routerId);

    T getOne(Q query, String routerId);

    <K> K getOne(Q query, Class<K> clazz, String routerId);

    boolean exists(Q query, String routerId);

    @SuppressWarnings("Carefully Using!!!")
    List<T> getAll(Q query, String routerId);

    <K> List<K> getAll(Q query, Class<K> clazz, String routerId);

    List<T> getList(Q query, String routerId);

    <K> List<K> getList(Q query, Class<K> clazz, String routerId);

    long estimatedCount(String routerId);

    long count(Q query, String routerId);

    T insert(T t, String routerId);

    List<T> insert(List<T> list, String routerId);

    T update(T t, String routerId);

    boolean updateFirst(HashMap<String, Object> queryMap, HashMap<String, Object> fieldMap, String routerId);

    boolean updateAll(HashMap<String, Object> queryMap, HashMap<String, Object> fieldMap, String routerId);

    void remove(String id, String routerId);

    void remove(Q query, String routerId);

    void buildIndex(Class clazz, String routerId);

    Query buildQuery(Q targetQuery);
}
