package com.scheduler.scheduler.service;

import com.scheduler.scheduler.model.ScheduledJobDefinition;

import java.util.List;

public interface ScheduleService {
    List<ScheduledJobDefinition> findAll();

    ScheduledJobDefinition findById(int id);

    ScheduledJobDefinition save(ScheduledJobDefinition scheduledJobDefinition);
}
