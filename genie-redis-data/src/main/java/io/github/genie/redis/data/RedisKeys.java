package io.github.genie.redis.data;

import io.github.genie.redis.data.api.RedisFloat;
import io.github.genie.redis.data.api.RedisHash;
import io.github.genie.redis.data.api.RedisKey;
import io.github.genie.redis.data.api.RedisList;
import io.github.genie.redis.data.api.RedisLock;
import io.github.genie.redis.data.api.RedisLong;
import io.github.genie.redis.data.api.RedisString;
import org.jetbrains.annotations.NotNull;

public interface RedisKeys {

    RedisKey getKey(@NotNull String key);

    RedisString getString(@NotNull String key);

    RedisHash getHash(@NotNull String key);

    RedisLong getLong(@NotNull String key);

    RedisFloat getFloat(@NotNull String key);

    RedisList getList(@NotNull String key);

    RedisLock getLock(@NotNull String key);

}
