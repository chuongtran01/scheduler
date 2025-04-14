package com.scheduler.scheduler.service;

import com.scheduler.scheduler.model.ScheduledJobDefinition;

import java.util.List;
import java.util.Set;

public interface SchedulerManager {
    void rescheduleJob(ScheduledJobDefinition scheduledJob);

    Set<String> getActiveJobs();

    List<String> getRunningJobs();

    ScheduledJobDefinition upsertJob(ScheduledJobDefinition scheduledJobDefinition);

    ScheduledJobDefinition findById(int id);

    void deleteJobById(int id);

    List<ScheduledJobDefinition> findAll();

    ScheduledJobDefinition save(ScheduledJobDefinition scheduledJobDefinition);
}