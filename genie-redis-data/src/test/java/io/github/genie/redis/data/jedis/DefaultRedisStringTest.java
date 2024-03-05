package io.github.genie.redis.data.jedis;

import io.github.genie.redis.data.RedisConfig;
import io.github.genie.redis.data.api.RedisString;
import io.github.genie.redis.data.api.TimeToLive;
import io.github.genie.redis.data.impl.DefaultRedisString;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRedisStringTest {

    private final RedisString string = new DefaultRedisString(
            new JedisCommand(RedisConfig.getJedisPooled()), randomString());

    @AfterEach
    void clear() {
        string.delete();
    }

    @Test
    void get() {
        String value = randomString();
        string.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        String s = this.string.get();
        Assertions.assertEquals(value, s);
    }

    private static String randomString() {
        return UUID.randomUUID().toString();
    }

    @Test
    void testGet() {
        TimeToLive timeToLive = string.timeToLive();
        assertTrue(timeToLive.isKeyNotExist());
        String value = randomString();
        string.set(value);
        long wait = TimeUnit.MILLISECONDS.toNanos(600);
        timeToLive = string.timeToLive();
        assertTrue(timeToLive.hasNoExpire());

        String getValue = string.get(Instant.ofEpochMilli(System.currentTimeMillis() + 1000));
        Assertions.assertEquals(value, getValue);

        System.out.println(string.timeToLive());

        LockSupport.parkNanos(wait);
        Assertions.assertEquals(value, string.get());


        LockSupport.parkNanos(wait);
        assertFalse(string.exists());
    }

    @Test
    void testGet1() {
        String value = randomString();
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
        String value = randomString();
        string.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        String getValue = string.getAndPersist();
        Assertions.assertEquals(value, getValue);
        TimeToLive ttl = string.timeToLive();
        assertTrue(ttl.hasNoExpire());
    }

    @Test
    void compareAndSet() {
        String value = randomString();
        string.set(value);
        String newValue = randomString();
        boolean b = string.compareAndSet(value, newValue);
        assertTrue(b);
        Assertions.assertEquals(string.get(), newValue);
        assertFalse(string.compareAndSet(value, value));
        Assertions.assertEquals(string.get(), newValue);
    }

    @Test
    void testCompareAndSet() {
        String value = randomString();
        string.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        String newValue = randomString();
        boolean seted = string.compareAndSet(value, newValue, SetExpiryOption.ofKeepTtl());
        TimeToLive timeToLive = string.timeToLive();
        assertTrue(timeToLive.getResult() > 0);
        assertTrue(seted);

        value = newValue;
        newValue = randomString();

        seted = string.compareAndSet(value, newValue, SetExpiryOption.ofExpire(Duration.ofMillis(100)));
        timeToLive = string.timeToLive();
        assertTrue(timeToLive.expiredAt() > System.currentTimeMillis());
        assertTrue(seted);

        LockSupport.parkNanos(Duration.ofMillis(100).toNanos());

        assertFalse(string.exists());

        string.persist();

        string.set(newValue);
        value = newValue;
        newValue = randomString();
        SetExpiryOption option = SetExpiryOption.ofExpireAtMillis(System.currentTimeMillis() + 1000);
        seted = string.compareAndSet(value, newValue, option);
        assertTrue(seted);
        timeToLive = string.timeToLive();
        assertTrue(timeToLive.expiredAt() > System.currentTimeMillis());
    }

    @Test
    void compareAndDelete() {
        String value = randomString();
        string.set(value);

        boolean deleted = string.compareAndDelete(randomString());
        assertFalse(deleted);
        assertEquals(value, string.get());
        deleted = string.compareAndDelete(value);
        assertTrue(deleted);
        assertFalse(string.exists());
    }

    @Test
    void getAndDelete() {
        String value = randomString();
        string.set(value);

        String delete = string.getAndDelete();
        assertEquals(value, delete);
        assertFalse(string.exists());
    }

    @Test
    void set() {
        String value = randomString();
        string.set(value);
        assertEquals(value, string.get());
        String newValue = randomString();
        boolean setSuccess = string.set(newValue, SetOption.ofNx());
        assertFalse(setSuccess);
        assertEquals(string.get(), value);
        setSuccess = string.set(newValue, SetOption.ofXx());
        assertTrue(setSuccess);
        assertEquals(string.get(), newValue);

        newValue = randomString();
        setSuccess = string.set(newValue, SetOption.ofExpireMillis(500));
        assertTrue(setSuccess);
        TimeToLive timeToLive = string.timeToLive();
        assertTrue(timeToLive.getResult() > 0);
        assertEquals(string.get(), newValue);
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500));
        assertFalse(string.exists());

        setSuccess = string.set(newValue, SetOption.ofExpireAtMillis(System.currentTimeMillis() + 500));
        assertTrue(setSuccess);
        timeToLive = string.timeToLive();
        System.out.println(timeToLive);
        assertTrue(timeToLive.getResult() > 0);
        assertEquals(string.get(), newValue);
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(600));
        assertFalse(string.exists());
    }

    @Test
    void getAndSet() {
        String value = randomString();
        String oldValue = string.getAndSet(value);
        assertNull(oldValue);
        assertEquals(value, string.get());
        String newValue = randomString();
        oldValue = string.getAndSet(newValue, SetOption.ofExpireMillis(1000));
        assertEquals(value, oldValue);
        assertEquals(newValue, string.get());
        TimeToLive timeToLive = string.timeToLive();
        assertTrue(timeToLive.getResult() > 0);
        value = newValue;
        newValue = randomString();
        oldValue = string.getAndSet(newValue, SetOption.ofKeepTtl());
        assertEquals(value, oldValue);
        assertEquals(newValue, string.get());
        assertTrue(string.timeToLive().getResult() > 0);
        assertTrue(string.timeToLive().getResult() <= timeToLive.getResult());

    }

    @Test
    void append() {
        String value = randomString();
        string.set(value);
        String append = randomString();
        string.append(append);
        assertEquals(string.get(), value + append);
    }

    @Test
    void getRange() {
        String value = randomString();
        string.set(value);
        String range = string.getRange(0, 2);
        assertEquals(value.substring(0, 3), range);
    }
}