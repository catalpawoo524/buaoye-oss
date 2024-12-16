package com.buaoye.oss.common.configuration;

import com.buaoye.oss.common.thread.BayThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自动装配配置类
 *
 * @author Jayson Wu
 * @since 2024-12-16
 */
@Configuration
public class BuaoyeOssConfiguration {

    @Primary
    @Bean(name = BayThreadPool.ASYNC_EXECUTOR)
    public Executor bayAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(BayThreadPool.CORE_POOL_SIZE);
        executor.setMaxPoolSize(BayThreadPool.MAX_POOL_SIZE);
        executor.setQueueCapacity(BayThreadPool.QUEUE_CAPACITY);
        executor.setThreadNamePrefix(BayThreadPool.NAME_PREFIX);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public BayThreadPool bayThreadPool() {
        return new BayThreadPool();
    }

}
