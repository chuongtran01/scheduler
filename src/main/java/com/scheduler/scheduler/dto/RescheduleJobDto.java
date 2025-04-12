package com.scheduler.scheduler.dto;

import jakarta.validation.constraints.NotBlank;

public record RescheduleJobDto(
        int id,
        @NotBlank
        String cronExpression
) {

}
