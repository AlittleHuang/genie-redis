package io.github.genie.redis.data.impl;

import io.github.genie.redis.data.RedisConfig;
import io.github.genie.redis.data.api.RedisString;
import io.github.genie.redis.data.api.TimeToLive;
import io.github.genie.redis.data.jedis.JedisCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRedisKeyTest {
    public static final String KEY = randomString();
    private final RedisString string = new DefaultRedisString(
            new JedisCommand(RedisConfig.getJedisPooled()), KEY);

    @AfterEach
    void clear() {
        string.delete();
    }

    @Test
    void key() {
        assertEquals(string.key(), KEY);
    }

    @Test
    void delete() {
        boolean delete = string.delete();
        assertFalse(delete);
        string.set(randomString());
        delete = string.delete();
        assertTrue(delete);
        assertFalse(string.delete());
    }

    @Test
    void exists() {
        assertFalse(string.exists());
        string.set(randomString());
        assertTrue(string.exists());
    }

    @Test
    void expire() {
        string.set(randomString());
    }

    @Test
    void persist() {
        boolean persist = string.persist();
        assertFalse(persist);
        string.set(randomString());
        string.expire(Duration.ofSeconds(1));
        TimeToLive timeToLive = string.timeToLive();
        System.out.println(timeToLive);
        assertTrue(timeToLive.getResult() > 0);
        persist = string.persist();
        assertTrue(persist);
        timeToLive = string.timeToLive();
        assertTrue(timeToLive.hasNoExpire());
    }

    private static String randomString() {
        return UUID.randomUUID().toString();
    }

}