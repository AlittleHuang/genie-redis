package io.github.genie.redis.data.jedis;

import io.github.genie.redis.data.command.RedisCommand;
import io.github.genie.redis.data.option.ExistOption;
import io.github.genie.redis.data.option.KeyExpiryOption;
import io.github.genie.redis.data.option.SetOption;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.SetParams;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JedisCommand implements RedisCommand {
    private final UnifiedJedis jedis;

    public JedisCommand(UnifiedJedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public long del(String... keys) {
        return jedis.del(keys);
    }

    @Override
    public boolean exists(@NotNull String key) {
        return jedis.exists(key);
    }

    @Override
    public boolean expire(@NotNull String key, @NotNull Duration duration) {
        return jedis.pexpire(key, duration.toMillis()) == 1;
    }

    @Override
    public boolean expire(@NotNull String key, @NotNull Duration duration, @NotNull KeyExpiryOption options) {
        var option = getExpiryOption(options);
        return jedis.pexpire(key, duration.toMillis(), option) != 0;
    }

    @Override
    public boolean expireAt(@NotNull String key, @NotNull Instant instant) {
        return jedis.pexpireAt(key, instant.toEpochMilli()) == 1;
    }

    @Override
    public boolean expireAt(@NotNull String key, @NotNull Instant instant, @NotNull KeyExpiryOption options) {
        var option = getExpiryOption(options);
        return jedis.pexpireAt(key, instant.toEpochMilli(), option) == 1;
    }

    @Override
    public boolean persist(@NotNull String key) {
        return jedis.persist(key) != 0;
    }

    @Override
    public long pttl(@NotNull String key) {
        return jedis.pttl(key);
    }

    @Override
    public String get(String key) {
        return jedis.get(key);
    }

    @Override
    public String set(String key, String value) {
        return jedis.set(key, value);
    }

    @Override
    public String setGet(String key, String value, SetOption option) {
        return jedis.setGet(key, value, of(option));
    }

    @Override
    public String setGet(String key, String value) {
        return jedis.setGet(key, value);
    }

    @Override
    public String set(String key, String value, SetOption option) {
        return jedis.set(key, value, of(option));
    }

    @Override
    public String getDel(String key) {
        return jedis.getDel(key);
    }

    @Override
    public long append(String key, String value) {
        return jedis.append(key, value);
    }

    @Override
    public String get(String key, Instant instant) {
        return jedis.getEx(key, GetExParams.getExParams().pxAt(instant.toEpochMilli()));
    }

    @Override
    public String get(String key, Duration duration) {
        return jedis.getEx(key, GetExParams.getExParams().px(duration.toMillis()));
    }

    @Override
    public String getAndPersist(String key) {
        return jedis.getEx(key, GetExParams.getExParams().persist());
    }

    @Override
    public Object eval(String script, int keyCount, String... params) {
        return jedis.eval(script, keyCount, params);
    }

    @Override
    public String getRange(String key, int start, int end) {
        return jedis.getrange(key, start, end);
    }

    @Override
    public String hget(String key, String field) {
        return jedis.hget(key, field);
    }

    @Override
    public boolean hexists(String key, String field) {
        return jedis.hexists(key, field);
    }

    @Override
    public Set<String> hkeys(String key) {
        return jedis.hkeys(key);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return jedis.hgetAll(key);
    }

    @Override
    public long hlen(String key) {
        return jedis.hlen(key);
    }

    @Override
    public List<String> hmget(String key, String[] fields) {
        return jedis.hmget(key, fields);
    }

    @Override
    public long hset(String key, String field, String value) {
        return jedis.hset(key, field, value);
    }

    @Override
    public long hsetnx(String key, String field, String value) {
        return jedis.hsetnx(key, field, value);
    }

    @Override
    public long hset(String key, Map<String, String> fields) {
        return jedis.hset(key, fields);
    }

    @Override
    public long hincrBy(String key, String field, long value) {
        return jedis.hincrBy(key, field, value);
    }

    @Override
    public double hincrByFloat(String key, String field, double value) {
        return jedis.hincrByFloat(key, field, value);
    }

    @Override
    public long hdel(String key, String... field) {
        return jedis.hdel(key, field);
    }

    private static redis.clients.jedis.args.ExpiryOption getExpiryOption(KeyExpiryOption options) {
        return switch (options) {
            case NX -> redis.clients.jedis.args.ExpiryOption.NX;
            case XX -> redis.clients.jedis.args.ExpiryOption.XX;
            case GT -> redis.clients.jedis.args.ExpiryOption.GT;
            case LT -> redis.clients.jedis.args.ExpiryOption.LT;
        };
    }

    private SetParams of(SetOption option) {
        SetParams params = SetParams.setParams();
        if (option.isKeepTtl()) {
            params = params.keepTtl();
        } else if (option.getExpire() != null) {
            params = params.px(option.getExpire().toMillis());
        } else if (option.getExpireAt() != null) {
            params = params.pxAt(option.getExpireAt().toEpochMilli());
        }
        ExistOption existOption = option.getExistOption();
        if (existOption == ExistOption.NX) {
            params = params.nx();
        } else if (existOption == ExistOption.XX) {
            params = params.xx();
        }
        return params;
    }
}
