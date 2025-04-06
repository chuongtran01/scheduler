package com.scheduler.scheduler.repository;

import com.scheduler.scheduler.model.ScheduledJobDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledJobDefinitionRepository extends JpaRepository<ScheduledJobDefinition, Integer> {
}
