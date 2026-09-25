package com.ttn.support.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncExecutionConfiguration {

    @Bean("knowledgeRefreshExecutor")
    @Profile("!test")
    TaskExecutor knowledgeRefreshExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("knowledge-refresh-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("knowledgeRefreshExecutor")
    @Profile("test")
    TaskExecutor synchronousKnowledgeRefreshExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean(destroyMethod = "shutdown")
    ExecutorService askExecutor() {
        return new ThreadPoolExecutor(
                2,
                8,
                30,
                java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(50),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
