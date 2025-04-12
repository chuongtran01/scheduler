-- liquibase formatted sql

-- changeset chuong.tran:scheduled-job
-- comment Create scheduled_job_definition table

CREATE TABLE scheduled_job_definition (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    job_name                VARCHAR(64) NOT NULL UNIQUE,
    active                  TINYINT(1) DEFAULT 1,
    log_start_stop_to_db    TINYINT(1) DEFAULT 1,
    log_start_stop_to_log   TINYINT(1) DEFAULT 1,
    cron_expression         VARCHAR(64) NOT NULL,
    last_start_date         TIMESTAMP,
    last_completed_date     TIMESTAMP,
    error_message           TEXT
);