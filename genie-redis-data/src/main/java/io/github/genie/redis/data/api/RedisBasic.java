package io.github.genie.redis.data.api;

import io.github.genie.redis.data.option.SetExpiryOption;
import io.github.genie.redis.data.option.SetOption;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public interface RedisBasic<T> extends RedisKey {

    T get();

    T getAndDelete();

    boolean set(@NotNull T value);

    T getAndSet(@NotNull T value);

    boolean set(@NotNull T value, SetOption option);

    T getAndSet(@NotNull T value, SetOption option);

    T get(Duration duration);

    T get(Instant instant);

    T getAndPersist();

    boolean compareAndSet(T expected, T newValue);

    boolean compareAndSet(T expected, T newValue, SetExpiryOption option);

    boolean compareAndDelete(T expected);

}
