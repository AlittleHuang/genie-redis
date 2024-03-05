package io.github.genie.redis.data;

import io.github.genie.redis.data.api.RedisDouble;
import io.github.genie.redis.data.api.RedisHash;
import io.github.genie.redis.data.api.RedisKey;
import io.github.genie.redis.data.api.RedisList;
import io.github.genie.redis.data.api.RedisLock;
import io.github.genie.redis.data.api.RedisLong;
import io.github.genie.redis.data.api.RedisString;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface RedisKeys {

    RedisKey getKey(@NotNull String key);

    RedisString getString(@NotNull String key);

    RedisHash getHash(@NotNull String key);

    RedisLong getLong(@NotNull String key);

    RedisDouble getFloat(@NotNull String key);

    RedisList getList(@NotNull String key);

    RedisLock getLock(@NotNull String key);

    List<String> getAllString(String... keys);

    boolean setAllString(Map<String, String> keysValues);

    boolean setAllStringNx(Map<String, String> keysValues);

}
