package io.github.genie.redis.data.impl;

import io.github.genie.redis.data.api.TimeToLive;

import java.time.Duration;

public class DefaultTimeToLive implements TimeToLive {
    // The command returns -2 if the key does not exist.
    // The command returns -1 if the key exists but has no associated expire.
    private static final int NO_EXPIRY = -1;
    private static final int NOT_EXIST = -2;

    private final long result;
    private final long createAt = System.currentTimeMillis();

    public DefaultTimeToLive(long result) {
        this.result = result;
    }

    @Override
    public long getResult() {
        return this.result;
    }

    @Override
    public long expiredAt() {
        return result + createAt;
    }

    @Override
    public boolean isKeyNotExist() {
        return result == NOT_EXIST;
    }

    @Override
    public boolean hasNoExpire() {
        return result == NO_EXPIRY;
    }

    @Override
    public String toString() {
        return Duration.ofMillis(result).toString();
    }
}
