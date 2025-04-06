package com.scheduler.scheduler.dto;

import jakarta.validation.constraints.NotBlank;

import java.sql.Timestamp;

public record ScheduledJobDefinitionDto(
        Integer id,
        @NotBlank
        String environment,
        @NotBlank
        String jobName,
        Boolean active,
        Boolean scheduled,
        Boolean logStartStopToDb,
        Boolean logStartStopToLog,
        @NotBlank
        String cronExpression,
        Timestamp lastStartDate,
        Timestamp lastCompletedDate,
        String errorMessage
) {
}
