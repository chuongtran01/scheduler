package com.scheduler.scheduler.dto;

import jakarta.validation.constraints.NotBlank;

public record UpsertScheduledJobDefinitionDto(
        Integer id,
        @NotBlank
        String jobName,
        boolean active,
        boolean logStartStopToDb,
        boolean logStartStopToLog,
        String cronExpression) {
}
