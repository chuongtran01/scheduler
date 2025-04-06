package com.scheduler.scheduler.service;

import com.scheduler.scheduler.model.ScheduledJobDefinition;
import com.scheduler.scheduler.repository.ScheduledJobDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduledJobDefinitionRepository scheduledJobDefinitionRepository;

    @Override
    public List<ScheduledJobDefinition> findAll() {
        return scheduledJobDefinitionRepository.findAll();
    }

    @Override
    public ScheduledJobDefinition findById(int id) {
        return scheduledJobDefinitionRepository.findById(id).orElse(null);
    }

    @Override
    public ScheduledJobDefinition save(ScheduledJobDefinition scheduledJobDefinition) {
        return scheduledJobDefinitionRepository.save(scheduledJobDefinition);
    }
}
