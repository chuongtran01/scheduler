package com.scheduler.scheduler.service;

import com.scheduler.scheduler.model.ScheduledJobDefinition;
import com.scheduler.scheduler.repository.ScheduledJobDefinitionRepository;
import com.scheduler.scheduler.scheduled.RunnableJob;
import jakarta.annotation.PostConstruct;
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
    @Override
    public void scheduleJobs() {
        Map<String, RunnableJob> classNameToBean = availableJobs.stream().collect(Collectors.toMap(job -> job.getClass().getSimpleName(), Function.identity()));

        List<ScheduledJobDefinition> jobs = findActiveScheduledJobs();

        logger.info(String.format("Total jobs: %d", jobs.size()));

        for (ScheduledJobDefinition job : jobs) {
            RunnableJob jobBean = classNameToBean.get(job.getJobName());
            if (jobBean == null) {
                logger.error("No matching job class found for: " + job.getJobName());
                continue;
            }

            Runnable task = this.wrapWithStatus(job, jobBean);

            CronTrigger trigger = new CronTrigger(job.getCronExpression());

            ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);
            scheduledTasks.put(job.getJobName(), future);
        }
    }

    @Override
    @Transactional
    public void rescheduleJob(ScheduledJobDefinition scheduledJob, String newCron) {
        scheduledJob.setCronExpression(newCron);
        scheduledJob.setActive(true);

        ScheduledJobDefinition savedScheduledJob = save(scheduledJob);

        RunnableJob jobBean = context.getBeansOfType(RunnableJob.class).values().stream()
                .filter(bean -> bean.getClass().getSimpleName().equals(scheduledJob.getJobName()))
                .findFirst()
                .orElse(null);

        if (jobBean == null) return;

        Runnable task = this.wrapWithStatus(savedScheduledJob, jobBean);

        ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(newCron));
        replaceScheduledTask(scheduledJob.getJobName(), future);
    }

    private Runnable wrapWithStatus(ScheduledJobDefinition scheduledJob, RunnableJob jobBean) {
        return () -> {
            String jobName = scheduledJob.getJobName();
            boolean logToDb = scheduledJob.getLogStartStopToDb();
            boolean logToLog = scheduledJob.getLogStartStopToLog();

            if (Boolean.TRUE.equals(jobRunningStatus.get(jobName))) {
                logger.info("Job {} is already running. Skipping...", jobName);
                return;
            }

            jobRunningStatus.put(jobName, true);
            try {
                if (logToLog) {
                    logger.info("Running job: {}", jobName);
                }
                if (logToDb) {
                    scheduledJob.setLastStartDate(Timestamp.valueOf(LocalDateTime.now()));
                }

                jobBean.run();

            } catch (Exception e) {
                String errorMessage = String.format("Error running job %s: %s", jobName, e.getMessage());
                logger.error(errorMessage, e);
                scheduledJob.setErrorMessage(errorMessage);
            } finally {
                jobRunningStatus.put(jobName, false);

                if (logToLog) {
                    logger.info("Finished job: {}", jobName);
                }
                if (logToDb) {
                    scheduledJob.setLastCompletedDate(Timestamp.valueOf(LocalDateTime.now()));
                }
                this.save(scheduledJob);
            }
        };
    }

    @Override
    public Set<String> getActiveJobs() {
        return scheduledTasks.keySet();
    }

    @Override
    public Set<String> getRunningJobs() {
        return jobRunningStatus.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public ScheduledJobDefinition upsertJob(ScheduledJobDefinition scheduledJobDefinition) {
        ScheduledJobDefinition scheduledJob = this.save(scheduledJobDefinition);

        if (scheduledJobDefinition.getActive()) {
            this.rescheduleJob(scheduledJob, scheduledJob.getCronExpression());
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

    private void replaceScheduledTask(String jobName, ScheduledFuture<?> future) {
        stopJob(jobName);
        scheduledTasks.put(jobName, future);
    }

}
