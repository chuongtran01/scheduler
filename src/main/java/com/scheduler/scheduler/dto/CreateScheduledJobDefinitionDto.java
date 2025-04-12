package com.scheduler.scheduler.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateScheduledJobDefinitionDto(
        @NotBlank
        String jobName,
        boolean active,
        boolean logStartStopToDb,
        boolean logStartStopToLog,
        String cronExpression,
        boolean isRegistered) {
}
