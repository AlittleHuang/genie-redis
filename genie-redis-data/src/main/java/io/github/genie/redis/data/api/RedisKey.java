package io.github.genie.redis.data.api;

import io.github.genie.redis.data.option.KeyExpiryOption;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public interface RedisKey {

    String key();

    /**
     * 删除键
     * @return 键是否存在
     */
    boolean delete();

    /**
     * 检查是否存在
     * @return 是否存在
     */
    boolean exists();

    /**
     * 设置过期时间
     * @param duration 时间间隔
     */
    boolean expire(@NotNull Duration duration);

    /**
     * 设置过期时间
     * @param duration 时间间隔
     * @param options 设置参数
     */
    boolean expire(@NotNull Duration duration, @NotNull KeyExpiryOption options);


    /**
     * 设置过期时间
     * @param instant 过期时间
     */
    boolean expire(@NotNull Instant instant);

    /**
     * 设置过期时间
     * @param instant 过期时间
     * @param options 设置参数
     */
    boolean expire(@NotNull Instant instant, @NotNull KeyExpiryOption options);

    /**
     * 移除过期时间
     */
    boolean persist();

    /**
     * 获取键过期时间
     *
     * @return 过期时间.
     */
    TimeToLive timeToLive();

}
