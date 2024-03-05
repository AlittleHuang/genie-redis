package io.github.genie.redis.data.impl;

import io.github.genie.redis.data.api.RedisDouble;
import io.github.genie.redis.data.api.RedisString;
import io.github.genie.redis.data.command.RedisCommand;

public class DefaultRedisDouble extends DefaultRedisNumber<Double> implements RedisDouble {
    public DefaultRedisDouble(RedisCommand jedis, RedisString string) {
        super(jedis, string);
    }

    @Override
    protected Double parseNoneNull(String value) {
        return Double.parseDouble(value);
    }

    @Override
    public double incrementBy(double value) {
        return command.incrementFlout(key, value);
    }
}
