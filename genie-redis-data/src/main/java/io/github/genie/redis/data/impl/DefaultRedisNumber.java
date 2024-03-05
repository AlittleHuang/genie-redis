package io.github.genie.redis.data.impl;

import io.github.genie.redis.data.api.RedisBasic;
import io.github.genie.redis.data.api.RedisString;
import io.github.genie.redis.data.command.RedisCommand;
import io.github.genie.redis.data.option.SetExpiryOption;
import io.github.genie.redis.data.option.SetOption;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public abstract class DefaultRedisNumber<T extends Number> extends DefaultRedisKey implements RedisBasic<T> {

    private final RedisString string;

    public DefaultRedisNumber(RedisCommand jedis, RedisString string) {
        super(jedis, string.key());
        this.string = string;
    }

    protected T parse(String value) {
        return value == null ? null : parseNoneNull(value);
    }

    abstract protected T parseNoneNull(String value);

    protected String format(T value) {
        return value == null ? null : formatNoneNull(value);
    }

    protected String formatNoneNull(T value) {
        return value.toString();
    }

    @Override
    public T get() {
        return parse(string.get());
    }

    @Override
    public T getAndDelete() {
        return parse(string.getAndDelete());
    }

    @Override
    public boolean set(@NotNull T value) {
        return string.set(format(value));
    }

    @Override
    public T getAndSet(@NotNull T value) {
        return parse(string.getAndSet(format(value)));
    }

    @Override
    public boolean set(@NotNull T value, SetOption option) {
        return string.set(format(value), option);
    }

    @Override
    public T getAndSet(@NotNull T value, SetOption option) {
        return parse(string.getAndSet(format(value), option));
    }

    @Override
    public T get(Duration duration) {
        return parse(string.get(duration));
    }

    @Override
    public T get(Instant instant) {
        return parse(string.get(instant));
    }

    @Override
    public T getAndPersist() {
        return parse(string.getAndPersist());
    }

    @Override
    public boolean compareAndSet(T expected, T newValue) {
        return string.compareAndSet(format(expected), format(newValue));
    }

    @Override
    public boolean compareAndSet(T expected, T newValue, SetExpiryOption option) {
        return string.compareAndSet(format(expected), format(newValue), option);
    }

    @Override
    public boolean compareAndDelete(T expected) {
        return string.compareAndDelete(format(expected));
    }

}
