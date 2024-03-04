package io.github.genie.redis.data.api;

public interface RedisString extends RedisBasic<String> {

    long append(String value);

    String getRange(int start, int end);


}
