package com.scheduler.scheduler.controller;

import com.scheduler.scheduler.dto.CreateScheduledJobDefinitionDto;
import com.scheduler.scheduler.dto.RescheduleJobDto;
import com.scheduler.scheduler.dto.ScheduledJobDefinitionDto;
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

    @PostMapping
    public ScheduledJobDefinitionDto create(@RequestBody @Valid CreateScheduledJobDefinitionDto scheduledJobDefinitionDto) {
        return scheduledJobDefinitionMapper.toScheduledJobDefinitionDto(schedulerManager.createJob(scheduledJobDefinitionMapper.toScheduledJobDefinition(scheduledJobDefinitionDto), scheduledJobDefinitionDto.isRegistered()));
    }

    @PostMapping("/deactivate/{id}")
    public void deactivateJobById(@PathVariable int id) {
        schedulerManager.deactivateJobById(id);
    }

    @PostMapping("/activate/{id}")
    public void activateJobById(@PathVariable int id) {
        schedulerManager.activateJobById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteJobById(@PathVariable int id) {
        schedulerManager.deleteJobById(id);
    }

    @GetMapping("/active")
    public Set<String> getActiveJobs() {
        return schedulerManager.getActiveJobs();
    }

    @PostMapping("/reschedule")
    public void rescheduleJobById(@RequestBody @Valid RescheduleJobDto rescheduleJobDto) {
        schedulerManager.rescheduleJob(rescheduleJobDto.id(), rescheduleJobDto.cronExpression());
    }
}
