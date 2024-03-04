package io.github.genie.redis.data.lock;

import io.github.genie.redis.data.log.Logs;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

class RedisSyncer {
    private static final Logger logger = Logs.getLogger(RedisSyncer.class);
    public static final int UNLOCK = -1;
    private final String key;
    private volatile long lockedTime = UNLOCK;
    private final LockOption option;
    private ScheduledFuture<?> renewalTask;

    public RedisSyncer(LockOption option, String key) {
        this.option = option;
        this.key = key;
    }

    public void lock() {
        while (true) {
            if (tryLock(Long.MAX_VALUE)) {
                return;
            }
        }
    }

    public boolean tryLock() {
        return tryLock(System.currentTimeMillis());
    }

    public boolean tryLock(long until) {
        AtomicReference<CompletableFuture<Void>> released = new AtomicReference<>(new CompletableFuture<>());
        Thread thread = Thread.currentThread();
        Cancelable subscribe = option.lockEvents.subscribe(key, () -> {
            logger.debug("waiting for released " + key + "[" + thread.getName() + "] success");
            CompletableFuture<Void> future = released.get();
            future.complete(null);
        });
        long waitTime = 0;
        try {
            do {
                logger.debug("waitTime: " + waitTime);
                await(waitTime, released);
                long keyExpireAt = acquireLock();
                if (isLocked()) {
                    return true;
                } else if (System.currentTimeMillis() >= until) {
                    return false;
                }
                waitTime = getWaitTime(until, keyExpireAt);
            } while (true);
        } finally {
            subscribe.cancel();
        }
    }

    private long acquireLock() {
        if (isLocked()) {
            logger.warn("{}:{} is locked when tryLock", key, option.clientId);
        }
        Object res = setNxOrPttl(key, option.clientId, option.ttl);
        setLocked("OK".equals(res));
        return res instanceof Long num
                ? num + System.currentTimeMillis()
                : Long.MAX_VALUE;
    }

    private void await(long waitTime, AtomicReference<CompletableFuture<Void>> released) {
        long start = System.currentTimeMillis();
        if (waitTime > 0) {
            try {
                released.get().get(waitTime, TimeUnit.MILLISECONDS);
                released.set(new CompletableFuture<>());
            } catch (TimeoutException e) {
                logger.debug("waiting for released {} timeout", key);
            } catch (Exception e) {
                logger.warn("waiting for released {} error", key, e);
            }
        }
        long wait = System.currentTimeMillis() - start;
        logger.debug("wait: {}", wait);
        if (wait > TimeUnit.SECONDS.toMillis(1)) {
            logger.debug("wait more than 1000 ms");
        }
    }

    public void setLocked(boolean locked) {
        if (locked) {
            lockedTime = System.currentTimeMillis();
            this.renewalTask = option.service.scheduleAtFixedRate(() -> {
                logger.debug("update ttl");
                option.command.pexpire(key, option.ttl);
            }, option.renewalPeriod, option.renewalPeriod, TimeUnit.MILLISECONDS);
        } else {
            lockedTime = UNLOCK;
            if (renewalTask != null) {
                renewalTask.cancel(false);
                renewalTask = null;
            }
        }
    }

    public boolean isLocked() {
        return lockedTime != UNLOCK;
    }

    private long getWaitTime(long deadline, long keyExpireAt) {
        long dead = deadline - System.currentTimeMillis();
        long expire = keyExpireAt - System.currentTimeMillis();
        long expected = expire > 0 && expire < dead ? expire : dead;
        return Math.min(expected, option.waitLimit);
    }

    public void unlock() {
        if (!isLocked()) {
            logger.warn(key + ":" + option.clientId + " is not locked when unlock");
        }
        if (compareAndDelete(key, option.clientId)) {
            option.lockEvents.release(key);
        } else {
            logger.warn(key + ":" + option.clientId + " delete none key when unlock");
        }
        setLocked(false);
    }

    public boolean compareAndDelete(String key, String value) {
        String script = "return {ARGV[1] == redis.call('get', KEYS[1]) and redis.call('del', KEYS[1]) or 0}";
        try {
            Object result = eval(script, key, value);
            logger.debug("compareAndDelete: " + result);
            return Long.valueOf(1).equals(result);
        } catch (Exception e) {
            logger.warn("eval `" + script + "` error", e);
            return true;
        }
    }

    public Object setNxOrPttl(String key, String value, long ttlMilliseconds) {
        String script = "return {redis.call('set', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2]) or redis.call('pttl', KEYS[1])}";
        try {
            Object result = eval(script, key, value, Long.toString(ttlMilliseconds));
            logger.debug("setNxOrPttl: " + result);
            return result;
        } catch (Exception e) {
            logger.warn("eval `" + script + "` error", e);
            return null;
        }
    }

    private Object eval(String script, String... params) {
        List<?> list = (List<?>) option.command.eval(script, 1, params);
        return list.getFirst();
    }

    @Override
    public String toString() {
        return "[" + System.identityHashCode(this) + ",+" + lockedTime + "]";
    }

    public long getHoldTime() {
        if (isLocked()) {
            return System.currentTimeMillis() - lockedTime;
        }
        throw new IllegalStateException();
    }

}
