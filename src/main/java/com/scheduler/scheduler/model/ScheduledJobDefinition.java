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
    private Boolean active;

    @Column(nullable = false)
    private Boolean scheduled;

    @Column(nullable = false)
    private Boolean logStartStopToDb;

    @Column(nullable = false)
    private Boolean logStartStopToLog;

    @Column(nullable = false, length = 64)
    private String cronExpression;

    private Timestamp lastStartDate;

    private Timestamp lastCompletedDate;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
