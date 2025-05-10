package com.scheduler.scheduler.scheduled;

import org.springframework.stereotype.Component;

@Component
public class SampleJob2 implements RunnableJob {
    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("SampleJob - 2");
    }
}
