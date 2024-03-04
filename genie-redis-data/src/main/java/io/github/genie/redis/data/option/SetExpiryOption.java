package io.github.genie.redis.data.option;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public class SetExpiryOption {

    public static SetExpiryOption of() {
        return new SetExpiryOption();
    }

    public static SetExpiryOption ofKeepTtl() {
        return of().keepTtl();
    }

    public static SetExpiryOption ofExpireAt(@NotNull Instant instant) {
        return of().expireAt(instant);
    }

    public static SetExpiryOption ofExpire(@NotNull Duration duration) {
        return of().expire(duration);
    }

    public SetExpiryOption keepTtl() {
        expireOption = KEEP_TTL;
        return this;
    }

    public SetExpiryOption expireAt(@NotNull Instant instant) {
        expireOption = instant;
        return this;
    }

    public SetExpiryOption expire(@NotNull Duration duration) {
        expireOption = duration;
        return this;
    }

    public Instant getExpireAt() {
        return expireOption instanceof Instant ? (Instant) expireOption : null;
    }

    public Duration getExpire() {
        return expireOption instanceof Duration ? (Duration) expireOption : null;
    }

    public boolean isKeepTtl() {
        return expireOption == KEEP_TTL;
    }

    private static final Object KEEP_TTL = new Object();

    private Object expireOption;

}
