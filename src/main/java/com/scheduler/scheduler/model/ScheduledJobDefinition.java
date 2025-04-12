package com.scheduler.scheduler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "scheduled_job_definition")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledJobDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 64)
    private String jobName;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean logStartStopToDb = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean logStartStopToLog = false;

    @Column(nullable = false, length = 64)
    private String cronExpression;

    private Timestamp lastStartDate;

    private Timestamp lastCompletedDate;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
