package com.scheduler.scheduler.service;

import com.scheduler.scheduler.model.ScheduledJobDefinition;
import com.scheduler.scheduler.repository.ScheduledJobDefinitionRepository;
import com.scheduler.scheduler.scheduled.RunnableJob;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SchedulerManagerServiceImpl implements SchedulerManager {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerManagerServiceImpl.class);
    private final List<RunnableJob> availableJobs;
    private final TaskScheduler taskScheduler;
    private final ApplicationContext context;
    private final ScheduledJobDefinitionRepository scheduledJobDefinitionRepository;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<String, Boolean> jobRunningStatus = new ConcurrentHashMap<>();

    public SchedulerManagerServiceImpl(List<RunnableJob> availableJobs, TaskScheduler taskScheduler, ApplicationContext context, ScheduledJobDefinitionRepository scheduledJobDefinitionRepository) {
        this.taskScheduler = taskScheduler;
        this.context = context;
        this.availableJobs = availableJobs;
        this.scheduledJobDefinitionRepository = scheduledJobDefinitionRepository;
    }

    @PostConstruct
    public void scheduleJobs() {
        Map<String, RunnableJob> classNameToBean = availableJobs.stream().collect(Collectors.toMap(job -> job.getClass().getSimpleName(), Function.identity()));

        List<ScheduledJobDefinition> jobs = findActiveScheduledJobs();

        logger.info(String.format("Total jobs: %d, Total running jobs: %d", jobs.size(), (long) jobs.stream().filter(ScheduledJobDefinition::getActive).count()));

        for (ScheduledJobDefinition job : jobs) {
            RunnableJob jobBean = classNameToBean.get(job.getJobName());
            if (jobBean == null) {
                logger.error("No matching job class found for: " + job.getJobName());
                continue;
            }

            Runnable task = this.wrapWithStatus(job, jobBean);

            CronTrigger trigger = new CronTrigger(job.getCronExpression());

            try {
                ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);
                scheduledTasks.put(job.getJobName(), future);
            } catch (Exception e) {
                logger.error("Failed to schedule job {}: {}", job.getJobName(), e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional
    public void rescheduleJob(ScheduledJobDefinition scheduledJob) {
        if (!scheduledJob.getActive()) {
            return;
        }

        String jobName = scheduledJob.getJobName();

        RunnableJob jobBean = context.getBeansOfType(RunnableJob.class).values().stream()
                .filter(bean -> bean.getClass().getSimpleName().equals(jobName))
                .findFirst()
                .orElse(null);

        if (jobBean == null) {
            logger.warn("Job class not found for '{}'. Skipping scheduling.", jobName);
            return;
        }


        Runnable task = this.wrapWithStatus(scheduledJob, jobBean);

        try {
            ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(scheduledJob.getCronExpression()));
            stopJob(jobName);
            scheduledTasks.put(jobName, future);
        } catch (Exception e) {
            logger.error("Failed to schedule job {}: {}", jobName, e.getMessage(), e);
        }
    }

    private Runnable wrapWithStatus(ScheduledJobDefinition scheduledJob, RunnableJob jobBean) {
        return () -> {
            String jobName = scheduledJob.getJobName();
            boolean logToDb = scheduledJob.getLogStartStopToDb();
            boolean logToLog = scheduledJob.getLogStartStopToLog();

            if (jobRunningStatus.putIfAbsent(jobName, true) != null) {
                logger.info("Job {} is already running. Skipping...", jobName);
                return;
            }

            Timestamp startTime = Timestamp.valueOf(LocalDateTime.now());
            Timestamp completedTime;
            String errorMessage = null;

            try {
                if (logToLog) {
                    logger.info(String.format("Running job: %s at %s", jobName, startTime));
                }

                jobBean.run();
            } catch (Exception e) {
                errorMessage = String.format("Error running job %s: %s", jobName, e.getMessage());
                logger.error(errorMessage, e);
            } finally {
                completedTime = Timestamp.valueOf(LocalDateTime.now());
                jobRunningStatus.remove(jobName);
                logger.info("[{}] Removed job {} from running status at {}", Thread.currentThread().getName(), jobName, Timestamp.valueOf(LocalDateTime.now()));

                if (logToLog) {
                    logger.info(String.format("Finished job: %s at %s", jobName, completedTime));
                }
                if (logToDb) {
                    scheduledJobDefinitionRepository.updateRunInfo(jobName, startTime, completedTime, errorMessage);
                }
            }
        };
    }

    @Override
    public Set<String> getActiveJobs() {
        return scheduledTasks.keySet();
    }

    @Override
    public List<String> getRunningJobs() {
        return jobRunningStatus.keySet().stream().toList();
    }

    @Override
    public ScheduledJobDefinition upsertJob(ScheduledJobDefinition scheduledJobDefinition) {
        ScheduledJobDefinition scheduledJob = this.save(scheduledJobDefinition);

        if (scheduledJobDefinition.getActive()) {
            this.rescheduleJob(scheduledJob);
        } else {
            this.stopJob(scheduledJob.getJobName());
        }
        return scheduledJob;
    }

    @Override
    public ScheduledJobDefinition save(ScheduledJobDefinition scheduledJobDefinition) {
        return scheduledJobDefinitionRepository.save(scheduledJobDefinition);
    }

    @Override
    public ScheduledJobDefinition findById(int id) {
        return scheduledJobDefinitionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public void deleteJobById(int id) {
        ScheduledJobDefinition scheduledJob = scheduledJobDefinitionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        scheduledJobDefinitionRepository.deleteJobById(id);
        stopJob(scheduledJob.getJobName());
    }

    @Override
    public List<ScheduledJobDefinition> findAll() {
        return scheduledJobDefinitionRepository.findAll();
    }

    private List<ScheduledJobDefinition> findActiveScheduledJobs() {
        return scheduledJobDefinitionRepository.findAllByActiveTrue();
    }

    private void stopJob(String jobName) {
        ScheduledFuture<?> future = scheduledTasks.get(jobName);

        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(jobName);
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down scheduler and cancelling all jobs...");
        scheduledTasks.values().forEach(future -> future.cancel(false));
        scheduledTasks.clear();
        jobRunningStatus.clear();
    }

}
