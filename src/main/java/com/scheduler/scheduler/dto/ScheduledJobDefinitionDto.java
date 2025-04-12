package com.scheduler.scheduler.dto;

import java.sql.Timestamp;

public record ScheduledJobDefinitionDto(
        Integer id,
        String jobName,
        Boolean active,
        Boolean logStartStopToDb,
        Boolean logStartStopToLog,
        String cronExpression,
        Timestamp lastStartDate,
        Timestamp lastCompletedDate,
        String errorMessage
) {
}
