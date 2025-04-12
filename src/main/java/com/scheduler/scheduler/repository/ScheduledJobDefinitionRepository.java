package com.scheduler.scheduler.repository;

import com.scheduler.scheduler.model.ScheduledJobDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduledJobDefinitionRepository extends JpaRepository<ScheduledJobDefinition, Integer> {
    List<ScheduledJobDefinition> findAllByActiveTrue();

    @Modifying
    @Query("UPDATE ScheduledJobDefinition sjd SET sjd.active = FALSE WHERE sjd.id = :id")
    void deactivateJobById(int id);

    @Modifying
    @Query("DELETE FROM ScheduledJobDefinition sjd WHERE sjd.id = :id")
    void deleteJobById(int id);
}
