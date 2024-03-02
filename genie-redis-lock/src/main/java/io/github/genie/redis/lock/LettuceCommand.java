package io.github.genie.redis.lock;

import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LettuceCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(LettuceCommand.class);
    private final StatefulRedisConnection<String, String> connect;
    private final StatefulRedisPubSubConnection<String, String> pubsub;

    public LettuceCommand(RedisClient redisClient) {
        this(redisClient.connect(), redisClient.connectPubSub());
    }

    public LettuceCommand(StatefulRedisConnection<String, String> connect,
                          StatefulRedisPubSubConnection<String, String> pubsub) {
        this.connect = connect;
        this.pubsub = pubsub;
    }

    @Override
    public Object eval(String script, int keyCount, String... params) {
        String[] keys = new String[keyCount];
        String[] values = new String[params.length - keyCount];
        System.arraycopy(params, 0, keys, 0, keys.length);
        System.arraycopy(params, keyCount, values, 0, values.length);
        return connect.sync().eval(script, ScriptOutputType.OBJECT, keys, values);
    }

    @Override
    public void publish(String channel, String key) {
        connect.sync().publish(channel, key);
    }

    @Override
    public void subscribe(String channel, Consumer<String> listener, CompletableFuture<Cancelable> completed) {
        CompletableFuture<Object> unsubscribed = new CompletableFuture<>();
        pubsub.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String ch, String message) {
                if (ch.equals(channel)) {
                    listener.accept(message);
                }
            }

            @Override
            public void unsubscribed(String ch, long count) {
                if (ch.equals(channel)) {
                    unsubscribed.complete(count);
                }
            }
        });
        RedisPubSubCommands<String, String> sync = pubsub.sync();
        sync.subscribe(channel);
        completed.complete(sync::unsubscribe);
        try {
            unsubscribed.join();
        } catch (Exception e) {
            logger.warn("unsubscribed done with error", e);
        }
    }

    @Override
    public void pexpire(String key, long milliseconds) {
        connect.sync().pexpire(key, milliseconds);
    }

}
