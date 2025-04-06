package com.scheduler.scheduler.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "scheduled_job_definition")
@Data
public class ScheduledJobDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 64)
    private String environment;

    @Column(nullable = false, length = 64)
    private String jobName;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean scheduled = false;

    @Column(nullable = false)
    private Boolean logStartStopToDb = false;

    @Column(nullable = false)
    private Boolean logStartStopToLog = false;

    @Column(nullable = false, length = 64)
    private String cronExpression;

    private Timestamp lastStartDate;

    private Timestamp lastCompletedDate;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
