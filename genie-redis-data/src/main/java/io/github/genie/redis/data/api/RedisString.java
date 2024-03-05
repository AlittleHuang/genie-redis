package io.github.genie.redis.data.api;

import org.jetbrains.annotations.NotNull;

public interface RedisString extends RedisBasic<String> {

    long append(@NotNull String value);

    String getRange(int start, int end);

    long setRange(int index, @NotNull String value);


}
