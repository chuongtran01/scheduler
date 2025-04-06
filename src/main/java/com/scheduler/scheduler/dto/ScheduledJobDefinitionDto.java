package com.scheduler.scheduler.dto;

import java.sql.Timestamp;

public record ScheduledJobDefinitionDto(
        Integer id,
        String environment,
        String jobName,
        Boolean active,
        Boolean scheduled,
        Boolean logStartStopToDb,
        Boolean logStartStopToLog,
        String cronExpression,
        Timestamp lastStartDate,
        Timestamp lastCompletedDate,
        String errorMessage
) {
}
