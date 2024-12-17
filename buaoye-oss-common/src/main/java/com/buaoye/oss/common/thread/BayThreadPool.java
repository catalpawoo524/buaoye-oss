package com.buaoye.oss.common.thread;

import com.buaoye.oss.common.exception.BuaoyeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * 线程池
 *
 * @author Jayson Wu
 * @since 2024-12-16
 */
public class BayThreadPool {

    private static final Logger log = LoggerFactory.getLogger(BayThreadPool.class);

    /**
     * 核心线程数
     */
    public static final int CORE_POOL_SIZE = 10;

    /**
     * 最大线程数
     */
    public static final int MAX_POOL_SIZE = 30;

    /**
     * 队列大小
     */
    public static final int QUEUE_CAPACITY = 1000;

    /**
     * 线程的名称前缀
     */
    public static final String NAME_PREFIX = "bay-thread-";

    /**
     * 线程池名称
     */
    public static final String BUAOYE_ASYNC_EXECUTOR = "bayAsyncExecutor";

    @Autowired
    @Qualifier(BUAOYE_ASYNC_EXECUTOR)
    private Executor bayAsyncExecutor;

    /**
     * 线程池任务分发，无返回值
     */
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("Buaoye Oss - 线程池任务分发异常");
                throw new BuaoyeException(e);
            }
        }, bayAsyncExecutor);
    }

    /**
     * 线程池任务分发，有返回值
     */
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.error("Buaoye Oss - 线程池任务分发异常");
                throw new BuaoyeException(e);
            }
        }, bayAsyncExecutor);
    }

    @PreDestroy
    public void shutdown() {
        if (bayAsyncExecutor instanceof ThreadPoolTaskExecutor) {
            ((ThreadPoolTaskExecutor) bayAsyncExecutor).shutdown();
            log.info("Buaoye Oss - 线程池关闭");
        }
    }

    public Executor getBayAsyncExecutor() {
        return bayAsyncExecutor;
    }

}
