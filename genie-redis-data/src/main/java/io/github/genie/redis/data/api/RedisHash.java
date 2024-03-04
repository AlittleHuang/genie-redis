package io.github.genie.redis.data.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisHash extends RedisKey {

    /**
     * 获取字段值
     *
     * @param field 字段名
     * @return 字段值
     */
    String get(String field);

    /**
     * 删除字段
     *
     * @param field 字段名
     * @return 字段值
     */
    long delete(String... field);

    /**
     * 检查字段是否存在
     *
     * @param field 字段名
     * @return 是否存在
     */
    boolean exist(String field);

    /**
     * 获取所有字段名
     *
     * @return 字段名集合
     */
    Set<String> getFields();

    /**
     * 获取所有字段名和字段值集合
     *
     * @return 字段名和字段值集合
     */
    Map<String, String> getAll();

    /**
     * 获取给定字段名的值
     *
     * @param keys 字段名集合
     * @return 字段值集合
     */
    List<String> getAll(String... keys);

    long length();

    /**
     * 设置字段值
     *
     * @param field 字段名
     * @param value 字段值
     * @return 是否产生了新的字段
     */
    boolean set(String field, String value);

    /**
     * 不存在则设置字段值
     *
     * @param field 字段名
     * @param value 字段值
     * @return 是否产生了新的字段
     */
    boolean setNx(String field, String value);


    /**
     * 设置多个字段值
     *
     * @param fields 字段名和字段值集合
     * @return 增加新的字段的数量
     */
    long set(Map<String, String> fields);

    /**
     * 给字段值加上一个整数
     *
     * @param field 字段名
     * @param value 加数
     * @return 增加后的结果
     */
    long incrementBy(String field, long value);

    /**
     * 给字段值加上一个浮点数
     *
     * @param field 字段名
     * @param value 加数
     * @return 增加后的结果
     */
    double incrementBy(String field, double value);


}
