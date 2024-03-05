package io.github.genie.redis.data.api;

public interface RedisDouble extends RedisBasic<Double> {

    double incrementBy(double value);

}
