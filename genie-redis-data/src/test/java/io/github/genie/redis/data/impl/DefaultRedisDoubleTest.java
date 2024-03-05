package io.github.genie.redis.data.impl;

import io.github.genie.redis.data.RedisConfig;
import io.github.genie.redis.data.api.RedisDouble;
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

class DefaultRedisDoubleTest {

    public static final JedisCommand COMMAND = new JedisCommand(RedisConfig.getJedisPooled());
    private final RedisDouble key = new DefaultRedisDouble(
            COMMAND, new DefaultRedisString(COMMAND, UUID.randomUUID().toString())
    );

    @AfterEach
    void clear() {
        key.delete();
    }

    @Test
    void get() {
        double value = randomDouble();
        key.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        Double s = this.key.get();
        Assertions.assertEquals(value, s);
    }

    private static double randomDouble() {
        return new Random().nextDouble();
    }

    @Test
    void testGet() {
        TimeToLive timeToLive = key.timeToLive();
        assertTrue(timeToLive.isKeyNotExist());
        double value = randomDouble();
        key.set(value);
        long wait = TimeUnit.MILLISECONDS.toNanos(600);
        timeToLive = key.timeToLive();
        assertTrue(timeToLive.hasNoExpire());

        Double getValue = key.get(Instant.ofEpochMilli(System.currentTimeMillis() + 800));
        Assertions.assertEquals(value, getValue);

        System.out.println(key.timeToLive());

        LockSupport.parkNanos(wait);
        Assertions.assertEquals(value, key.get());


        LockSupport.parkNanos(wait);
        assertFalse(key.exists());
    }

    @Test
    void testGet1() {
        Double value = randomDouble();
        key.set(value);
        long wait = TimeUnit.MILLISECONDS.toNanos(500);

        Double getValue = key.get(Duration.ofSeconds(1));
        Assertions.assertEquals(value, getValue);

        System.out.println(key.timeToLive());

        LockSupport.parkNanos(wait);
        Assertions.assertEquals(value, key.get());


        LockSupport.parkNanos(wait);
        Assertions.assertNull(key.get());
    }

    @Test
    void getAndPersist() {
        Double value = randomDouble();
        key.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        Double getValue = key.getAndPersist();
        Assertions.assertEquals(value, getValue);
        TimeToLive ttl = key.timeToLive();
        assertTrue(ttl.hasNoExpire());
    }

    @Test
    void compareAndSet() {
        Double value = randomDouble();
        key.set(value);
        Double newValue = randomDouble();
        boolean b = key.compareAndSet(value, newValue);
        assertTrue(b);
        Assertions.assertEquals(key.get(), newValue);
        assertFalse(key.compareAndSet(value, value));
        Assertions.assertEquals(key.get(), newValue);
    }

    @Test
    void testCompareAndSet() {
        double value = randomDouble();
        key.set(value, SetOption.ofExpire(Duration.ofSeconds(15)));
        double newValue = randomDouble();
        boolean seted = key.compareAndSet(value, newValue, SetExpiryOption.ofKeepTtl());
        TimeToLive timeToLive = key.timeToLive();
        assertTrue(timeToLive.getResult() > 0);
        assertTrue(seted);

        value = newValue;
        newValue = randomDouble();

        seted = key.compareAndSet(value, newValue, SetExpiryOption.ofExpire(Duration.ofMillis(100)));
        timeToLive = key.timeToLive();
        assertTrue(timeToLive.expiredAt() > System.currentTimeMillis());
        assertTrue(seted);

        LockSupport.parkNanos(Duration.ofMillis(100).toNanos());

        assertFalse(key.exists());

        key.persist();

        key.set(newValue);
        value = newValue;
        newValue = randomDouble();
        SetExpiryOption option = SetExpiryOption.ofExpireAtMillis(System.currentTimeMillis() + 1000);
        seted = key.compareAndSet(value, newValue, option);
        assertTrue(seted);
        timeToLive = key.timeToLive();
        assertTrue(timeToLive.expiredAt() > System.currentTimeMillis());
    }

    @Test
    void compareAndDelete() {
        Double value = randomDouble();
        key.set(value);

        boolean deleted = key.compareAndDelete(randomDouble());
        assertFalse(deleted);
        assertEquals(value, key.get());
        deleted = key.compareAndDelete(value);
        assertTrue(deleted);
        assertFalse(key.exists());
    }

    @Test
    void getAndDelete() {
        Double value = randomDouble();
        key.set(value);

        Double delete = key.getAndDelete();
        assertEquals(value, delete);
        assertFalse(key.exists());
    }

    @Test
    void set() {
        Double value = randomDouble();
        key.set(value);
        assertEquals(value, key.get());
        Double newValue = randomDouble();
        boolean setSuccess = key.set(newValue, SetOption.ofNx());
        assertFalse(setSuccess);
        assertEquals(key.get(), value);
        setSuccess = key.set(newValue, SetOption.ofXx());
        assertTrue(setSuccess);
        assertEquals(key.get(), newValue);

        newValue = randomDouble();
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
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(900));
        assertFalse(key.exists());
    }

    @Test
    void getAndSet() {
        Double value = randomDouble();
        Double oldValue = key.getAndSet(value);
        assertNull(oldValue);
        assertEquals(value, key.get());
        Double newValue = randomDouble();
        oldValue = key.getAndSet(newValue, SetOption.ofExpireMillis(1000));
        assertEquals(value, oldValue);
        assertEquals(newValue, key.get());
        TimeToLive timeToLive = key.timeToLive();
        assertTrue(timeToLive.getResult() > 0);
        value = newValue;
        newValue = randomDouble();
        oldValue = key.getAndSet(newValue, SetOption.ofKeepTtl());
        assertEquals(value, oldValue);
        assertEquals(newValue, key.get());
        assertTrue(key.timeToLive().getResult() > 0);
        assertTrue(key.timeToLive().getResult() <= timeToLive.getResult());

    }


    @Test
    void incrementBy() {
        double a = randomDouble();
        key.set(a);
        double b = randomDouble();
        double newValue = key.incrementBy(b);
        Assertions.assertEquals(newValue, a + b);
    }
}