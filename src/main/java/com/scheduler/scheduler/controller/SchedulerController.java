package com.scheduler.scheduler.controller;

import com.scheduler.scheduler.dto.ScheduledJobDefinitionDto;
import com.scheduler.scheduler.dto.UpsertScheduledJobDefinitionDto;
import com.scheduler.scheduler.mapper.ScheduledJobDefinitionMapper;
import com.scheduler.scheduler.service.SchedulerManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {
    private final SchedulerManager schedulerManager;

    private final ScheduledJobDefinitionMapper scheduledJobDefinitionMapper;

    @GetMapping
    public List<ScheduledJobDefinitionDto> findAll() {
        return schedulerManager.findAll().stream().map(scheduledJobDefinitionMapper::toScheduledJobDefinitionDto).toList();
    }

    @GetMapping("/{id}")
    public ScheduledJobDefinitionDto findById(@PathVariable int id) {
        return scheduledJobDefinitionMapper.toScheduledJobDefinitionDto(schedulerManager.findById(id));
    }

    @PatchMapping
    public ScheduledJobDefinitionDto upsert(@RequestBody @Valid UpsertScheduledJobDefinitionDto scheduledJobDefinitionDto) {
        return scheduledJobDefinitionMapper.toScheduledJobDefinitionDto(schedulerManager.upsertJob(scheduledJobDefinitionMapper.toScheduledJobDefinition(scheduledJobDefinitionDto)));
    }

    @DeleteMapping("/{id}")
    public void deleteJobById(@PathVariable int id) {
        schedulerManager.deleteJobById(id);
    }

    @GetMapping("/active")
    public Set<String> getActiveJobs() {
        return schedulerManager.getActiveJobs();
    }

    @GetMapping("/running")
    public Set<String> getRunningJobs() {
        return schedulerManager.getRunningJobs();
    }
}
