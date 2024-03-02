package io.github.genie.redis.lock;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

public class LockEvents {
    private static final Logger logger = LoggerFactory.getLogger(LockEvents.class);
    private final Map<String, Set<Runnable>> listeners = new ConcurrentHashMap<>();
    private final Command command;
    private final String channelId;

    private volatile boolean running = true;
    private volatile CompletableFuture<Cancelable> subscribed = new CompletableFuture<>();

    public LockEvents(Command command, String channelId) {
        this.command = command;
        this.channelId = channelId;
        Thread runner = new Thread(this::subscribe, "subscribe");
        runner.start();
        subscribed.join();
    }

    public Cancelable subscribe(String key, Runnable action) {
        // noinspection FunctionalExpressionCanBeFolded
        Runnable runnable = action::run;
        Set<Runnable> set = listeners.computeIfAbsent(key, newKeySet());
        set.add(runnable);
        return () -> set.remove(runnable);
    }

    @NotNull
    private static Function<String, Set<Runnable>> newKeySet() {
        return k -> ConcurrentHashMap.newKeySet();
    }

    public void close() {
        running = false;
        if (subscribed != null) {
            subscribed.join().cancel();
        }
    }

    private void subscribe() {
        while (running) {
            command.subscribe(channelId, string -> {
                logger.trace("receive released {}", string);
                Set<Runnable> set = listeners.getOrDefault(string, Collections.emptySet());
                for (Runnable runnable : set) {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.warn(e.getLocalizedMessage(), e);
                    }
                }
            }, subscribed);
            subscribed = new CompletableFuture<>();
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(3));
        }
    }

    public void release(String key) {
        logger.trace("publish released {}", key);
        command.publish(channelId, key);
    }
}
