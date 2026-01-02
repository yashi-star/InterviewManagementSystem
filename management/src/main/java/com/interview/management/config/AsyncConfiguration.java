package com.interview.management.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration
 * Enables async processing for AI screening tasks
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfiguration {
    
    /**
     * Task executor for async AI screening
     * Configured based on application.properties:
     * - core-size: 2 (minimum threads)
     * - max-size: 5 (maximum threads)
     * - queue-capacity: 100 (max queued tasks)
     */
    @Bean(name = "aiScreeningExecutor")
    public Executor aiScreeningExecutor() {
        log.info("Initializing AI Screening Task Executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-screening-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Rejection policy: CallerRunsPolicy
        // If queue is full, the calling thread will execute the task
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        
        log.info("AI Screening Executor initialized - Core: 2, Max: 5, Queue: 100");
        return executor;
    }
}