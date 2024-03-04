package io.github.genie.redis.data.api;

public interface TimeToLive {

    long getResult();

    long getExpiredAt();

    boolean isNotExist();

    boolean isNoExpired();

}
