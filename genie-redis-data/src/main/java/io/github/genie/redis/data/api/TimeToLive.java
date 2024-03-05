package io.github.genie.redis.data.api;

public interface TimeToLive {

    long getResult();

    long expiredAt();

    boolean isKeyNotExist();

    boolean hasNoExpire();

}
