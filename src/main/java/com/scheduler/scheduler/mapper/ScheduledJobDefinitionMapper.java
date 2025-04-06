package com.scheduler.scheduler.mapper;

import com.scheduler.scheduler.dto.ScheduledJobDefinitionDto;
import com.scheduler.scheduler.model.ScheduledJobDefinition;
import org.mapstruct.Mapper;

@Mapper
public interface ScheduledJobDefinitionMapper {
    ScheduledJobDefinitionDto toDto(ScheduledJobDefinition entity);

    ScheduledJobDefinition toEntity(ScheduledJobDefinitionDto dto);
}
