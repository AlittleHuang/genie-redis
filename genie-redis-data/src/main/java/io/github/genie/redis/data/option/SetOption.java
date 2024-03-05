package io.github.genie.redis.data.option;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public class SetOption {

    ExistOption existOption;
    SetExpiryOption expiryOption = SetExpiryOption.of();

    public SetOption nx() {
        existOption = ExistOption.NX;
        return this;
    }

    public SetOption xx() {
        existOption = ExistOption.XX;
        return this;
    }

    public SetOption keepTtl() {
        expiryOption.keepTtl();
        return this;
    }

    public SetOption expireAt(@NotNull Instant instant) {
        expiryOption.expireAt(instant);
        return this;
    }

    public SetOption expire(@NotNull Duration duration) {
        expiryOption.expire(duration);
        return this;
    }

    public SetOption expireAtMillis(long instant) {
        expiryOption.expireAtMillis(instant);
        return this;
    }

    public SetOption expireMillis(long duration) {
        expiryOption.expireMillis(duration);
        return this;
    }

    public static SetOption of() {
        return new SetOption();
    }

    public static SetOption ofKeepTtl() {
        return of().keepTtl();
    }

    public static SetOption ofExpireAt(@NotNull Instant instant) {
        return of().expireAt(instant);
    }

    public static SetOption ofExpire(@NotNull Duration duration) {
        return of().expire(duration);
    }

    public static SetOption ofExpireAtMillis(long instant) {
        return ofExpireAt(Instant.ofEpochMilli(instant));
    }

    public static SetOption ofExpireMillis(long duration) {
        return ofExpire(Duration.ofMillis(duration));
    }

    public static SetOption ofNx() {
        return of().nx();
    }

    public static SetOption ofXx() {
        return of().xx();
    }

    public ExistOption getExistOption() {
        return existOption;
    }

    public Instant getExpireAt() {
        return expiryOption.getExpireAt();
    }

    public Duration getExpire() {
        return expiryOption.getExpire();
    }

    public boolean isKeepTtl() {
        return expiryOption.isKeepTtl();
    }
}
