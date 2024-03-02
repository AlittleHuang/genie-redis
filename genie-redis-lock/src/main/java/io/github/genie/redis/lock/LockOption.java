package io.github.genie.redis.lock;

import io.lettuce.core.RedisClient;
import redis.clients.jedis.UnifiedJedis;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LockOption {

    public static final String DEFAULT_ID = makeId();

    public static final long WAIT_LIMIT = TimeUnit.SECONDS.toMillis(10);

    public static final long DEFAULT_TTL = TimeUnit.SECONDS.toMillis(90);

    public static final long DEFAULT_RENEWAL_PERIOD = DEFAULT_TTL / 3 * 2;
    /**
     * <pre>
     * public static final ScheduledExecutorService DEFAULT_SERVICE = Executors.newSingleThreadScheduledExecutor(r -> {
     *     Thread thread = new Thread(r);
     *     thread.setName("lock-event-loop");
     *     thread.setDaemon(true);
     *     return thread;
     * });
     * </pre>
     */
    public static final ScheduledExecutorService DEFAULT_SERVICE = Executors.newSingleThreadScheduledExecutor(
            Thread.ofVirtual()
                    .name("lock-event-loop")
                    .factory()
    );

    public static final Executor DEFAULT_COMMAND_ASYNC_EXECUTOR =
            command -> Thread.ofVirtual().name("async-command-executor").start(command);


    final String clientId;
    final Command command;
    final ScheduledExecutorService scheduler;
    final Executor commandAsyncExecutor;
    final LockEvents lockEvents;
    final long ttl;
    final long renewalPeriod;
    final long waitLimit;

    public LockOption(String clientId,
                      Command command,
                      ScheduledExecutorService scheduler,
                      Executor commandAsyncExecutor,
                      LockEvents lockEvents,
                      long ttl,
                      long renewalPeriod,
                      long waitLimit) {
        this.clientId = clientId;
        this.command = command;
        this.scheduler = scheduler;
        this.commandAsyncExecutor = commandAsyncExecutor;
        this.lockEvents = lockEvents;
        this.ttl = ttl;
        this.renewalPeriod = renewalPeriod;
        this.waitLimit = waitLimit;
    }

    public static Builder builder(RedisClient command) {
        return builder(new LettuceCommand(command));
    }

    public static Builder builder(UnifiedJedis command) {
        return builder(new JedisCommand(command));
    }

    public static Builder builder(Command command) {
        return new Builder()
                .setId(DEFAULT_ID)
                .setService(DEFAULT_SERVICE)
                .setCommandAsyncExecutor(DEFAULT_COMMAND_ASYNC_EXECUTOR)
                .setTtl(DEFAULT_TTL)
                .setRenewalPeriod(DEFAULT_RENEWAL_PERIOD)
                .setWaitLimit(WAIT_LIMIT)
                .setCommand(command);
    }

    public static class Builder {
        String clientId;
        Command command;
        ScheduledExecutorService scheduler;
        Executor commandAsyncExecutor;
        long ttl;
        long renewalPeriod;
        long waitLimit;

        Builder() {
        }

        public Builder setService(ScheduledExecutorService service) {
            this.scheduler = service;
            return this;
        }

        public Builder setCommand(Command command) {
            this.command = command;
            return this;
        }

        public Builder setId(String id) {
            this.clientId = id;
            return this;
        }

        public Builder setTtl(long ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder setRenewalPeriod(long renewalPeriod) {
            this.renewalPeriod = renewalPeriod;
            return this;
        }

        public Builder setWaitLimit(long waitLimit) {
            this.waitLimit = waitLimit;
            return this;
        }

        public Builder setCommandAsyncExecutor(Executor commandAsyncExecutor) {
            this.commandAsyncExecutor = commandAsyncExecutor;
            return this;
        }

        public LockOption build() {
            return new LockOption(
                    clientId,
                    command,
                    scheduler,
                    commandAsyncExecutor,
                    new LockEvents(command, clientId),
                    ttl,
                    renewalPeriod,
                    waitLimit
            );
        }
    }


    private static String makeId() {
        byte[] id = new byte[Long.BYTES * 2];
        new SecureRandom().nextBytes(id);
        return Base64.getEncoder().withoutPadding().encodeToString(id);
    }
}
