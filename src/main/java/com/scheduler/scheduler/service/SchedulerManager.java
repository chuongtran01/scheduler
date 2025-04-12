package com.scheduler.scheduler.service;

import com.scheduler.scheduler.model.ScheduledJobDefinition;

import java.util.List;
import java.util.Set;

public interface SchedulerManager {
    void scheduleJobs();

    void rescheduleJob(int id, String newCron);

    Set<String> getActiveJobs();

    ScheduledJobDefinition createJob(ScheduledJobDefinition scheduledJobDefinition, boolean isRegistered);

    ScheduledJobDefinition findById(int id);

    void deactivateJobById(int id);

    void activateJobById(int id);

    void deleteJobById(int id);

    List<ScheduledJobDefinition> findAll();
}