package io.github.genie.redis.data.jedis;

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
    public long getExpiredAt() {
        return result + createAt;
    }

    @Override
    public boolean isNotExist() {
        return result == NOT_EXIST;
    }

    @Override
    public boolean isNoExpired() {
        return result == NO_EXPIRY;
    }

    @Override
    public String toString() {
        return Duration.ofMillis(result).toString();
    }
}
