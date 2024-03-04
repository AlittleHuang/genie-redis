package io.github.genie.redis.data.lock;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class RedisLock implements Lock {

    private final ReentrantLock lock = new ReentrantLock();
    private final RedisSyncer sync;

    RedisLock(RedisSyncer sync) {
        this.sync = sync;
    }

    @Override
    public void lock() {
        lock.lock();
        if (!sync.isLocked()) {
            sync.lock();
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock.lockInterruptibly();
        if (!sync.isLocked()) {
            sync.lock();
        }
    }

    @Override
    public boolean tryLock() {
        boolean locked = false;
        if (lock.tryLock()) {
            try {
                locked = sync.isLocked() || sync.tryLock();
            } finally {
                if (!locked) lock.unlock();
            }
        }
        return locked;
    }

    @Override
    public boolean tryLock(long time, @NotNull TimeUnit unit) throws InterruptedException {
        long until = System.currentTimeMillis() + unit.toMillis(time);
        boolean locked = false;
        if (lock.tryLock(time, unit)) {
            try {
                locked = sync.isLocked() || sync.tryLock(until);
            } finally {
                if (!locked) lock.unlock();
            }
        }
        return locked;
    }

    @Override
    public void unlock() {
        int holdCount = lock.getHoldCount();
        boolean async = false;
        try {
            if (holdCount == 1 && sync.isLocked()) {
                if (sync.getHoldTime() > 64) {
                    unlockSync();
                } else {
                    async = true;
                }
            }
        } finally {
            lock.unlock();
        }
        if (async) {
            unlockAsync();
        }
    }

    private void unlockAsync() {
        Thread.startVirtualThread(() -> {
            lock.lock();
            try {
                unlockSync();
            } finally {
                lock.unlock();
            }
        });
    }

    private void unlockSync() {
        if (lock.getHoldCount() == 1 && sync.isLocked()) {
            sync.unlock();
        }
    }

    @NotNull
    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

}
