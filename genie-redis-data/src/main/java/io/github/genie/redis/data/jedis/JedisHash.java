package io.github.genie.redis.data.jedis;

import io.github.genie.redis.data.api.RedisHash;

import java.util.List;
import java.util.Map;
import java.util.Set;

class JedisHash extends DefaultRedisKey implements RedisHash {

    public JedisHash(JedisCommand jedis, String key) {
        super(jedis, key);
    }

    @Override
    public String get(String field) {
        return command.hget(key, field);
    }

    @Override
    public long delete(String... field) {
        return command.hdel(key, field);
    }

    @Override
    public boolean exist(String field) {
        return command.hexists(key, field);
    }

    @Override
    public Set<String> getFields() {
        return command.hkeys(key);
    }

    @Override
    public Map<String, String> getAll() {
        return command.hgetAll(key);
    }

    @Override
    public long length() {
        return command.hlen(key);
    }

    @Override
    public List<String> getAll(String... fields) {
        return command.hmget(key, fields);
    }

    @Override
    public boolean set(String field, String value) {
        return command.hset(key, field, value) != 0;
    }

    @Override
    public boolean setNx(String field, String value) {
        return command.hsetnx(key, field, value) != 0;
    }

    @Override
    public long set(Map<String, String> fields) {
        return command.hset(key, fields);
    }

    @Override
    public long incrementBy(String field, long value) {
        return command.hincrBy(key, field, value);
    }

    @Override
    public double incrementBy(String field, double value) {
        return command.hincrByFloat(key, field, value);
    }

}
