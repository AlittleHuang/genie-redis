package io.github.genie.redis.lock;

import io.lettuce.core.RedisClient;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.UnifiedJedis;

import java.util.concurrent.locks.Lock;

public class LockFactory implements AutoCloseable {
    private final WeakCache<Lock> cache;
    private final LockOption option;

    public Lock get(@NotNull String key) {
        return cache.get(key);
    }

    public LockFactory(LockOption option) {
        this.option = option;
        cache = new WeakCache<>(this::createLock, option.scheduler);
    }

    public LockFactory(UnifiedJedis command) {
        this(LockOption.builder(command).build());
    }

    public LockFactory(RedisClient command) {
        this(LockOption.builder(command).build());
    }

    private Lock createLock(String key) {
        return new RedisLock(new RedisSyncer(option, key));
    }

    public void close() {
        option.lockEvents.close();
        cache.close();
    }
}
