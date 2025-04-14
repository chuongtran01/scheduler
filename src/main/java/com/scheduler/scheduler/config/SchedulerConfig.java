package com.scheduler.scheduler.config;

import com.scheduler.scheduler.properties.SchedulerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {
    private final SchedulerProperties schedulerProperties;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(schedulerProperties.getPoolSize()); // Max concurrent jobs
        scheduler.setThreadNamePrefix("job-scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}
