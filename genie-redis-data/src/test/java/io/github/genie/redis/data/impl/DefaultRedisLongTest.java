package io.github.genie.redis.data.impl;

import io.github.genie.redis.data.RedisConfig;
import io.github.genie.redis.data.api.RedisLong;
import io.github.genie.redis.data.api.TimeToLive;
import io.github.genie.redis.data.jedis.JedisCommand;
import io.github.genie.redis.data.option.SetExpiryOption;
import io.github.genie.redis.data.option.SetOption;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRedisLongTest {

    public static final JedisCommand COMMAND = new JedisCommand(RedisConfig.getJedisPooled());
    private final RedisLong key = new DefaultRedisLong(
            COMMAND, new DefaultRedisString(COMMAND, UUID.randomUUID().toString())
    );

    @AfterEach
    void clear() {
        key.delete();
    }

    @Test
    void get() {
        long value = randomLong();
        key.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        Long s = this.key.get();
        Assertions.assertEquals(value, s);

        value = Long.MAX_VALUE;
        key.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        Assertions.assertEquals(value, key.get());

        value = Long.MIN_VALUE;
        key.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        Assertions.assertEquals(value, key.get());
    }

    private static long randomLong() {
        return new Random().nextInt();
    }

    @Test
    void testGet() {
        TimeToLive timeToLive = key.timeToLive();
        assertTrue(timeToLive.isKeyNotExist());
        long value = randomLong();
        key.set(value);
        long wait = TimeUnit.MILLISECONDS.toNanos(900);
        timeToLive = key.timeToLive();
        assertTrue(timeToLive.hasNoExpire());

        Long getValue = key.get(Instant.ofEpochMilli(System.currentTimeMillis() + 800));
        Assertions.assertEquals(value, getValue);

        System.out.println(key.timeToLive());

        LockSupport.parkNanos(wait);
        Assertions.assertEquals(value, key.get());


        LockSupport.parkNanos(wait);
        assertFalse(key.exists());
    }

    @Test
    void testGet1() {
        Long value = randomLong();
        key.set(value);
        long wait = TimeUnit.MILLISECONDS.toNanos(500);

        Long getValue = key.get(Duration.ofSeconds(1));
        Assertions.assertEquals(value, getValue);

        System.out.println(key.timeToLive());

        LockSupport.parkNanos(wait);
        Assertions.assertEquals(value, key.get());


        LockSupport.parkNanos(wait);
        Assertions.assertNull(key.get());
    }

    @Test
    void getAndPersist() {
        Long value = randomLong();
        key.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        Long getValue = key.getAndPersist();
        Assertions.assertEquals(value, getValue);
        TimeToLive ttl = key.timeToLive();
        assertTrue(ttl.hasNoExpire());
    }

    @Test
    void compareAndSet() {
        Long value = randomLong();
        key.set(value);
        Long newValue = randomLong();
        boolean b = key.compareAndSet(value, newValue);
        assertTrue(b);
        Assertions.assertEquals(key.get(), newValue);
        assertFalse(key.compareAndSet(value, value));
        Assertions.assertEquals(key.get(), newValue);
    }

    @Test
    void testCompareAndSet() {
        long value = randomLong();
        key.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        long newValue = randomLong();
        boolean seted = key.compareAndSet(value, newValue, SetExpiryOption.ofKeepTtl());
        TimeToLive timeToLive = key.timeToLive();
        assertTrue(timeToLive.getResult() > 0);
        assertTrue(seted);

        value = newValue;
        newValue = randomLong();

        seted = key.compareAndSet(value, newValue, SetExpiryOption.ofExpire(Duration.ofMillis(100)));
        timeToLive = key.timeToLive();
        assertTrue(timeToLive.expiredAt() > System.currentTimeMillis());
        assertTrue(seted);

        LockSupport.parkNanos(Duration.ofMillis(100).toNanos());

        assertFalse(key.exists());

        key.persist();

        key.set(newValue);
        value = newValue;
        newValue = randomLong();
        SetExpiryOption option = SetExpiryOption.ofExpireAtMillis(System.currentTimeMillis() + 1000);
        seted = key.compareAndSet(value, newValue, option);
        assertTrue(seted);
        timeToLive = key.timeToLive();
        assertTrue(timeToLive.expiredAt() > System.currentTimeMillis());
    }

    @Test
    void compareAndDelete() {
        Long value = randomLong();
        key.set(value);

        boolean deleted = key.compareAndDelete(randomLong());
        assertFalse(deleted);
        assertEquals(value, key.get());
        deleted = key.compareAndDelete(value);
        assertTrue(deleted);
        assertFalse(key.exists());
    }

    @Test
    void getAndDelete() {
        Long value = randomLong();
        key.set(value);

        Long delete = key.getAndDelete();
        assertEquals(value, delete);
        assertFalse(key.exists());
    }

    @Test
    void set() {
        Long value = randomLong();
        key.set(value);
        assertEquals(value, key.get());
        Long newValue = randomLong();
        boolean setSuccess = key.set(newValue, SetOption.ofNx());
        assertFalse(setSuccess);
        assertEquals(key.get(), value);
        setSuccess = key.set(newValue, SetOption.ofXx());
        assertTrue(setSuccess);
        assertEquals(key.get(), newValue);

        newValue = randomLong();
        setSuccess = key.set(newValue, SetOption.ofExpireMillis(500));
        assertTrue(setSuccess);
        TimeToLive timeToLive = key.timeToLive();
        assertTrue(timeToLive.getResult() > 0);
        assertEquals(key.get(), newValue);
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500));
        assertFalse(key.exists());

        setSuccess = key.set(newValue, SetOption.ofExpireAtMillis(System.currentTimeMillis() + 500));
        assertTrue(setSuccess);
        timeToLive = key.timeToLive();
        System.out.println(timeToLive);
        assertTrue(timeToLive.getResult() > 0);
        assertEquals(key.get(), newValue);
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
        assertFalse(key.exists());
    }

    @Test
    void getAndSet() {
        Long value = randomLong();
        Long oldValue = key.getAndSet(value);
        assertNull(oldValue);
        assertEquals(value, key.get());
        Long newValue = randomLong();
        oldValue = key.getAndSet(newValue, SetOption.ofExpireMillis(1000));
        assertEquals(value, oldValue);
        assertEquals(newValue, key.get());
        TimeToLive timeToLive = key.timeToLive();
        assertTrue(timeToLive.getResult() > 0);
        value = newValue;
        newValue = randomLong();
        oldValue = key.getAndSet(newValue, SetOption.ofKeepTtl());
        assertEquals(value, oldValue);
        assertEquals(newValue, key.get());
        assertTrue(key.timeToLive().getResult() > 0);
        assertTrue(key.timeToLive().getResult() <= timeToLive.getResult());

    }


    @Test
    void incrementBy() {
        long a = randomLong();
        key.set(a);
        long b = randomLong();
        long newValue = key.incrementBy(b);
        Assertions.assertEquals(newValue, a + b);
    }

    @Test
    void decrement() {
        long a = randomLong();
        key.set(a);
        long newValue = key.decrement();
        Assertions.assertEquals(newValue, a - 1);
    }

    @Test
    void decrementBy() {
        long a = randomLong();
        key.set(a);
        long b = randomLong();
        long newValue = key.decrementBy(b);
        Assertions.assertEquals(newValue, a - b);
    }

    @Test
    void increment() {
        long a = randomLong();
        key.set(a);
        long newValue = key.increment();
        Assertions.assertEquals(newValue, a + 1);
    }
}