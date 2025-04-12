package com.scheduler.scheduler.service;

import com.scheduler.scheduler.model.ScheduledJobDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SchedulerManager {
    void scheduleJobs();

    void rescheduleJob(ScheduledJobDefinition scheduledJob);

    Set<String> getActiveJobs();

    Map<String, Boolean> getJobStatuses();

    ScheduledJobDefinition upsertJob(ScheduledJobDefinition scheduledJobDefinition);

    ScheduledJobDefinition findById(int id);

    void deleteJobById(int id);

    List<ScheduledJobDefinition> findAll();

    ScheduledJobDefinition save(ScheduledJobDefinition scheduledJobDefinition);
}