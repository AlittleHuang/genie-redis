package io.github.genie.redis.data.api;

public interface Binary extends RedisBasic<byte[]> {

    int getBit(int index);

}
