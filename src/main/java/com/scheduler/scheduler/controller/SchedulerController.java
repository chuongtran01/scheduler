package com.scheduler.scheduler.controller;

import com.scheduler.scheduler.dto.ScheduledJobDefinitionDto;
import com.scheduler.scheduler.mapper.ScheduledJobDefinitionMapper;
import com.scheduler.scheduler.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {
    private final ScheduleService scheduleService;

    private final ScheduledJobDefinitionMapper scheduledJobDefinitionMapper;

    @GetMapping
    public List<ScheduledJobDefinitionDto> findAll() {
        return scheduleService.findAll().stream().map(scheduledJobDefinitionMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ScheduledJobDefinitionDto findById(@PathVariable int id) {
        return scheduledJobDefinitionMapper.toDto(scheduleService.findById(id));
    }

    @PostMapping
    public ScheduledJobDefinitionDto upsert(ScheduledJobDefinitionDto scheduledJobDefinitionDto) {
        return scheduledJobDefinitionMapper.toDto(scheduleService.save(scheduledJobDefinitionMapper.toEntity(scheduledJobDefinitionDto)));
    }
}
