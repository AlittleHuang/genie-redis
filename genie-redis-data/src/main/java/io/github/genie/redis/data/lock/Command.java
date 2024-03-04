package io.github.genie.redis.data.lock;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Command {

    Object eval(String script, int keyCount, String... params);

    void publish(String channel, String key);

    void subscribe(String channel, Consumer<String> listener, CompletableFuture<Cancelable> completed);

    void pexpire(String key, long ttl);
}
