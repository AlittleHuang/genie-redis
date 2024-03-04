package io.github.genie.redis.data.jedis;

import io.github.genie.redis.data.RedisConfig;
import io.github.genie.redis.data.api.RedisString;
import io.github.genie.redis.data.api.TimeToLive;
import io.github.genie.redis.data.option.SetOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

class DefaultRedisStringTest {

    private final RedisString string = new DefaultRedisString(
            new JedisCommand(RedisConfig.getJedisPooled()), randomUuid());


    @Test
    void get() {
        String value = randomUuid();
        string.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        String s = this.string.get();
        Assertions.assertEquals(value, s);
    }

    private static String randomUuid() {
        return UUID.randomUUID().toString();
    }

    @Test
    void testGet() {
        String value = randomUuid();
        string.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        long wait = TimeUnit.MILLISECONDS.toNanos(1500);

        String getValue = string.get(Instant.ofEpochMilli(System.currentTimeMillis() + 2000));
        Assertions.assertEquals(value, getValue);

        System.out.println(string.timeToLive());

        LockSupport.parkNanos(wait);
        Assertions.assertEquals(value, string.get());


        LockSupport.parkNanos(wait);
        Assertions.assertNull(string.get());
    }

    @Test
    void testGet1() {
        String value = randomUuid();
        string.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        long wait = TimeUnit.MILLISECONDS.toNanos(500);

        String getValue = string.get(Duration.ofSeconds(1));
        Assertions.assertEquals(value, getValue);

        System.out.println(string.timeToLive());

        LockSupport.parkNanos(wait);
        Assertions.assertEquals(value, string.get());


        LockSupport.parkNanos(wait);
        Assertions.assertNull(string.get());
    }

    @Test
    void getAndPersist() {
        String value = randomUuid();
        string.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        String getValue = string.getAndPersist();
        Assertions.assertEquals(value, getValue);
        TimeToLive ttl = string.timeToLive();
        Assertions.assertTrue(ttl.isNoExpired());
        string.delete();
    }

    @Test
    void compareAndSet() {
    }

    @Test
    void testCompareAndSet() {
    }

    @Test
    void compareAndDelete() {
    }

    @Test
    void getAndDelete() {
    }

    @Test
    void set() {
    }

    @Test
    void getAndSet() {
    }

    @Test
    void testSet() {
    }

    @Test
    void testGetAndSet() {
    }

    @Test
    void append() {
    }

    @Test
    void getRange() {
    }
}