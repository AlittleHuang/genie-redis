package io.github.genie.redis.data.option;

public enum KeyExpiryOption {
    /**
     * 没有过期时间
     */
    NX,
    /**
     * 有过期时间
     */
    XX,
    /**
     * 新时间大于现有
     */
    GT,
    /**
     * 没有过期时间或者新时间小于现有
     */
    LT
}
