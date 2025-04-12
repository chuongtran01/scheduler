package com.scheduler.scheduler.mapper;

import com.scheduler.scheduler.dto.CreateScheduledJobDefinitionDto;
import com.scheduler.scheduler.dto.ScheduledJobDefinitionDto;
import com.scheduler.scheduler.model.ScheduledJobDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ScheduledJobDefinitionMapper {
    ScheduledJobDefinitionDto toScheduledJobDefinitionDto(ScheduledJobDefinition entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastStartDate", ignore = true)
    @Mapping(target = "lastCompletedDate", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    ScheduledJobDefinition toScheduledJobDefinition(CreateScheduledJobDefinitionDto dto);
}
