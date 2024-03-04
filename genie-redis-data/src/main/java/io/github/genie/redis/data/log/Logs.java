package io.github.genie.redis.data.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logs {
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz.getName());
    }
}
