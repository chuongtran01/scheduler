package com.scheduler.scheduler.scheduled;

import org.springframework.stereotype.Component;

@Component
public class SampleJob implements RunnableJob {
    @Override
    public void run() {
        System.out.println("SampleJob");
    }
}
