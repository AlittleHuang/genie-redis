package io.github.genie.redis.data.api;

public interface RedisLong extends RedisBasic<Long> {

    long decrement();

    long decrementBy(long value);

    long increment();

    long incrementBy(long value);

}
