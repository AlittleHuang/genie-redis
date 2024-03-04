package io.github.genie.redis.data.option;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public class SetOption extends SetExpiryOption {

    ExistOption existOption;

    public SetOption nx() {
        existOption = ExistOption.NX;
        return this;
    }

    public SetOption xx() {
        existOption = ExistOption.XX;
        return this;
    }

    @Override
    public SetOption keepTtl() {
        return (SetOption) super.keepTtl();
    }

    @Override
    public SetOption expireAt(@NotNull Instant instant) {
        return (SetOption) super.expireAt(instant);
    }

    @Override
    public SetOption expire(@NotNull Duration duration) {
        return (SetOption) super.expire(duration);
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

    public static SetOption ofNx() {
        return of().nx();
    }

    public static SetOption ofXx() {
        return of().xx();
    }

    public ExistOption getExistOption() {
        return existOption;
    }

}
