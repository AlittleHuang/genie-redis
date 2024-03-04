package io.github.genie.redis.data.lock;

import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.UnifiedJedis;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class JedisCommand implements Command {

    private final UnifiedJedis unifiedJedis;

    public JedisCommand(UnifiedJedis unifiedJedis) {
        this.unifiedJedis = unifiedJedis;
    }

    @Override
    public Object eval(String script, int keyCount, String... params) {
        return unifiedJedis.eval(script, keyCount, params);
    }

    @Override
    public void publish(String channel, String key) {
        unifiedJedis.publish(channel, key);
    }

    @Override
    public void subscribe(String channel, Consumer<String> listener, CompletableFuture<Cancelable> completed) {
        JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                listener.accept(message);
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                completed.complete(this::unsubscribe);
            }
        };
        unifiedJedis.subscribe(pubSub, channel);
    }

    @Override
    public void pexpire(String key, long milliseconds) {
        unifiedJedis.pexpire(key, milliseconds);
    }

}
