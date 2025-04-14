package com.scheduler.scheduler.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "scheduler")
@Getter
@Setter
@Component
public class SchedulerProperties {
    private int poolSize;
}
