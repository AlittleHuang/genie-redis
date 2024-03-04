package io.github.genie.redis.data.command;

import io.github.genie.redis.data.option.ExpiryOption;
import io.github.genie.redis.data.option.SetOption;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("SpellCheckingInspection")
public interface RedisCommand {

    Duration TTL_NO_EXPIRE = Duration.ofMillis(-1);
    Duration TTL_KEY_NOT_EXIST = Duration.ofMillis(-2);

    /**
     * 删除键
     *
     * @param keys 键
     * @return 被删除的数量
     */
    long del(String... keys);

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    boolean exists(@NotNull String key);

    /**
     * 设置过期时间
     *
     * @param key      键
     * @param duration 时间间隔
     */
    boolean expire(@NotNull String key, @NotNull Duration duration);

    /**
     * 设置过期时间
     *
     * @param key      键
     * @param duration 时间间隔
     * @param options  设置参数
     */
    boolean expire(@NotNull String key, @NotNull Duration duration, @NotNull ExpiryOption options);

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param instant 过期时间
     */
    boolean expireAt(@NotNull String key, @NotNull Instant instant);

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param instant 过期时间
     * @param options 设置参数
     */
    boolean expireAt(@NotNull String key, @NotNull Instant instant, @NotNull ExpiryOption options);

    /**
     * 移除过期时间
     *
     * @param key 键
     */
    boolean persist(@NotNull String key);

    /**
     * 获取键过期时间
     *
     * @param key 键
     * @return 过期时间.
     */
    long pttl(@NotNull String key);

    // hash

    String hget(String key, String field);

    boolean hexists(String key, String field);

    Set<String> hkeys(String key);

    Map<String, String> hgetAll(String key);

    long hlen(String key);

    List<String> hmget(String key, String[] fields);

    long hset(String key, String field, String value);

    long hsetnx(String key, String field, String value);

    long hset(String key, Map<String, String> fields);

    long hincrBy(String key, String field, long value);

    double hincrByFloat(String key, String field, double value);

    long hdel(String key, String... field);

    // string
    String get(String key);

    String set(String key, String value);

    String setGet(String key, String value, SetOption option);

    String setGet(String key, String value);

    String set(String key, String value, SetOption option);

    String getDel(String key);

    long append(String key, String value);

    String get(String key, Instant instant);

    String get(String key, Duration duration);

    String getAndPersist(String key);

    Object eval(String script, int keyCount, String... args);

    String getRange(String key, int start, int end);
}
