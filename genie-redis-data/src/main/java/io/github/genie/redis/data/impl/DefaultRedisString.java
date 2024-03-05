package io.github.genie.redis.data.impl;

import io.github.genie.redis.data.api.RedisString;
import io.github.genie.redis.data.jedis.JedisCommand;
import io.github.genie.redis.data.option.SetExpiryOption;
import io.github.genie.redis.data.option.SetOption;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public class DefaultRedisString extends DefaultRedisKey implements RedisString {

    private static final String SCRIPT_CAS_TTL =
            "return ARGV[1] == redis.call('get', KEYS[1]) and redis.call('set', KEYS[1], ARGV[2], 'KEEPTTL') or 0";
    private static final String SCRIPT_CAS_PX =
            "return ARGV[1] == redis.call('get', KEYS[1]) and redis.call('set', KEYS[1], ARGV[2], 'PX', ARGV[3]) or 0";
    String SCRIPT_CAS_PX_AT =
            "return ARGV[1] == redis.call('get', KEYS[1]) and redis.call('set', KEYS[1], ARGV[2], 'PXAT', ARGV[3]) or 0";

    public DefaultRedisString(JedisCommand jedis, String key) {
        super(jedis, key);
    }

    @Override
    public String get() {
        return command.get(key);
    }

    @Override
    public String get(Instant instant) {
        return command.get(key, instant);
    }

    @Override
    public String get(Duration duration) {
        return command.get(key, duration);
    }

    @Override
    public String getAndPersist() {
        return command.getAndPersist(key);
    }

    @Override
    public boolean compareAndSet(String expected, String newValue) {
        String script = "return ARGV[1] == redis.call('get', KEYS[1]) and redis.call('set', KEYS[1], ARGV[2])";
        Object result = command.eval(script, 1, key, expected, newValue);
        return "OK".equals(result);
    }

    @Override
    public boolean compareAndSet(String expected, String newValue, SetExpiryOption option) {
        if (option == null) {
            return compareAndSet(expected, newValue);
        }
        String OK = "OK";
        if (option.isKeepTtl()) {
            return OK.equals(command.eval(SCRIPT_CAS_TTL, 1, key, expected, newValue));
        } else if (option.getExpire() != null) {
            String millis = Long.toString(option.getExpire().toMillis());
            return OK.equals(command.eval(SCRIPT_CAS_PX, 1, key, expected, newValue, millis));
        } else if (option.getExpireAt() != null) {
            String millis = Long.toString(option.getExpireAt().toEpochMilli());
            return OK.equals(command.eval(SCRIPT_CAS_PX_AT, 1, key, expected, newValue, millis));
        } else {
            return compareAndSet(expected, newValue);
        }
    }

    @Override
    public boolean compareAndDelete(String expected) {
        String script = "return ARGV[1] == redis.call('get', KEYS[1]) and redis.call('del', KEYS[1]) or 0";
        return Long.valueOf(1).equals(command.eval(script, 1, key, expected));
    }

    @Override
    public String getAndDelete() {
        return command.getDel(key);
    }

    @Override
    public boolean set(@NotNull String value) {
        return "OK".equals(command.set(key, value));
    }

    @Override
    public String getAndSet(@NotNull String value) {
        return command.setGet(key, value);
    }

    @Override
    public boolean set(@NotNull String value, SetOption option) {
        return "OK".equals(command.set(key, value, option));
    }

    @Override
    public String getAndSet(@NotNull String value, SetOption option) {
        return command.setGet(key, value, option);
    }

    @Override
    public long append(String value) {
        return command.append(key, value);
    }

    @Override
    public String getRange(int start, int end) {
        return command.getRange(key, start, end);
    }
}
