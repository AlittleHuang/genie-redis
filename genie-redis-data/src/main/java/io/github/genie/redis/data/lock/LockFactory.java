package io.github.genie.redis.data.lock;

import io.lettuce.core.RedisClient;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.UnifiedJedis;

import java.util.concurrent.locks.Lock;

public class LockFactory implements AutoCloseable {
    private final WeakCache<Lock> cache = new WeakCache<>(this::createLock);
    private final LockOption option;

    public Lock get(@NotNull String key) {
        return cache.get(key);
    }

    public LockFactory(LockOption option) {
        this.option = option;
    }

    public LockFactory(UnifiedJedis command) {
        this.option = new LockOption.Builder(command).build();
    }

    public LockFactory(RedisClient command) {
        this.option = new LockOption.Builder(command).build();
    }

    private Lock createLock(String key) {
        return new RedisLock(new RedisSyncer(option, key));
    }

    public void close() {
        option.lockEvents.close();
        cache.close();
    }
}
