package io.github.genie.redis.data.impl;

import io.github.genie.redis.data.api.RedisKey;
import io.github.genie.redis.data.api.TimeToLive;
import io.github.genie.redis.data.command.RedisCommand;
import io.github.genie.redis.data.option.KeyExpiryOption;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public class DefaultRedisKey implements RedisKey {
    protected final RedisCommand command;
    protected final String key;

    public DefaultRedisKey(RedisCommand jedis, String key) {
        this.command = jedis;
        this.key = key;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public boolean delete() {
        return command.del(key) == 1;
    }

    @Override
    public boolean exists() {
        return command.exists(key);
    }

    @Override
    public boolean expire(@NotNull Duration duration) {
        return command.expire(key, duration);
    }

    @Override
    public boolean expire(@NotNull Duration duration, @NotNull KeyExpiryOption options) {
        return command.expire(key, duration, options);
    }

    @Override
    public boolean expire(@NotNull Instant instant) {
        return command.expireAt(key, instant);
    }

    @Override
    public boolean expire(@NotNull Instant instant, @NotNull KeyExpiryOption options) {
        return command.expireAt(key, instant, options);
    }

    @Override
    public boolean persist() {
        return command.persist(key);
    }

    @Override
    public TimeToLive timeToLive() {
        return new DefaultTimeToLive(command.pttl(key));
    }

}
