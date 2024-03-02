package io.github.genie.redis.lock;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class WeakCache<T> {

    private final Map<String, IdentifiableRef<T>> cache = new ConcurrentHashMap<>();
    private final ReferenceQueue<T> expiredQueue = new ReferenceQueue<>();
    private final Function<String, T> constructor;
    private final ScheduledFuture<?> scheduled;

    public WeakCache(Function<String, T> constructor, ScheduledExecutorService scheduler) {
        this.constructor = constructor;
        this.scheduled = scheduler.scheduleAtFixedRate(
                this::clear, 1, 1, TimeUnit.SECONDS);
    }

    public T get(@NotNull String key) {
        AtomicReference<T> result = new AtomicReference<>();
        cache.compute(key, (k, ref) -> {
            if (ref != null) {
                result.set(ref.get());
            }
            if (result.get() == null) {
                T newValue = newInstance(k);
                result.set(newValue);
                ref = new IdentifiableRef<>(k, newValue, expiredQueue);
            }
            return ref;
        });
        return result.get();
    }

    @NotNull
    private T newInstance(String key) {
        return constructor.apply(key);
    }

    private void clear() {
        while (true) {
            Reference<?> remove = expiredQueue.poll();
            if (remove instanceof IdentifiableRef<?> ref) {
                cache.remove(ref.id, ref);
            } else if (remove == null) {
                return;
            }
        }
    }

    public void close() {
        scheduled.cancel(false);
    }

    public void forEach(BiConsumer<? super String, ? super T> action) {
        cache.forEach((s, tIdentifiableRef) -> {
            T value = tIdentifiableRef.get();
            if (value != null) {
                action.accept(s, value);
            }
        });
    }

    public void forEachValue(Consumer<? super T> action) {
        cache.forEach((s, tIdentifiableRef) -> {
            T value = tIdentifiableRef.get();
            if (value != null) {
                action.accept(value);
            }
        });
    }

    static class IdentifiableRef<T> extends WeakReference<T> {
        public final String id;

        public IdentifiableRef(String id, T referent, ReferenceQueue<? super T> q) {
            super(referent, q);
            this.id = id;
        }
    }

}
