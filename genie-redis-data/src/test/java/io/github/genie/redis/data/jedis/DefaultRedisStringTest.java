package io.github.genie.redis.data.jedis;

import io.github.genie.redis.data.RedisConfig;
import io.github.genie.redis.data.api.RedisString;
import io.github.genie.redis.data.api.TimeToLive;
import io.github.genie.redis.data.option.SetExpiryOption;
import io.github.genie.redis.data.option.SetOption;
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void clear() {
        string.delete();
    }


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
        string.set(value);
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
        string.set(value);
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
    }

    @Test
    void compareAndSet() {
        String value = randomUuid();
        string.set(value);
        String newValue = randomUuid();
        boolean b = string.compareAndSet(value, newValue);
        Assertions.assertTrue(b);
        Assertions.assertEquals(string.get(), newValue);
        Assertions.assertFalse(string.compareAndSet(value, value));
        Assertions.assertEquals(string.get(), newValue);
    }

    @Test
    void testCompareAndSet() {
        String value = randomUuid();
        string.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        String newValue = randomUuid();
        boolean b = string.compareAndSet(value, newValue, SetExpiryOption.ofKeepTtl());
        TimeToLive timeToLive = string.timeToLive();
        Assertions.assertTrue(timeToLive.getExpiredAt() > System.currentTimeMillis());

        value = newValue;
        newValue = randomUuid();

        b = string.compareAndSet(value, newValue, SetExpiryOption.ofExpire(Duration.ofMillis(100)));
        timeToLive = string.timeToLive();
        Assertions.assertTrue(timeToLive.getExpiredAt() > System.currentTimeMillis());
        Assertions.assertTrue(b);

        LockSupport.parkNanos(Duration.ofMillis(100).toNanos());

        Assertions.assertFalse(string.exists());

        string.persist();

        string.set(newValue);
        value = newValue;
        newValue = randomUuid();
        SetExpiryOption option = SetExpiryOption.ofExpireAtMillis(System.currentTimeMillis() + 1000);
        b = string.compareAndSet(value, newValue, option);
        Assertions.assertTrue(b);
        timeToLive = string.timeToLive();
        Assertions.assertTrue(timeToLive.getExpiredAt() > System.currentTimeMillis());


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