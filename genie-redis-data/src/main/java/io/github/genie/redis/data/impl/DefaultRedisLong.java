package io.github.genie.redis.data.impl;

import io.github.genie.redis.data.api.RedisLong;
import io.github.genie.redis.data.api.RedisString;
import io.github.genie.redis.data.command.RedisCommand;

public class DefaultRedisLong extends DefaultRedisNumber<Long> implements RedisLong {

    public DefaultRedisLong(RedisCommand jedis, RedisString string) {
        super(jedis, string);
    }

    @Override
    protected Long parseNoneNull(String value) {
        return Long.parseLong(value);
    }

    @Override
    public long decrement() {
        return command.decrement(key);
    }

    @Override
    public long decrementBy(long value) {
        return command.decrementBy(key, value);
    }

    @Override
    public long increment() {
        return command.increment(key);
    }

    @Override
    public long incrementBy(long value) {
        return command.incrementBy(key, value);
    }
}
