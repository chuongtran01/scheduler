package com.scheduler.scheduler.service;

import com.scheduler.scheduler.model.ScheduledJobDefinition;
import com.scheduler.scheduler.repository.ScheduledJobDefinitionRepository;
import com.scheduler.scheduler.scheduled.RunnableJob;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SchedulerManagerServiceImpl implements SchedulerManager {
    private final List<RunnableJob> availableJobs;
    private final TaskScheduler taskScheduler;
    private final ApplicationContext context;

    private final ScheduledJobDefinitionRepository scheduledJobDefinitionRepository;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();


    public SchedulerManagerServiceImpl(List<RunnableJob> availableJobs, TaskScheduler taskScheduler, ApplicationContext context, ScheduledJobDefinitionRepository scheduledJobDefinitionRepository) {
        this.taskScheduler = taskScheduler;
        this.context = context;
        this.availableJobs = availableJobs;
        this.scheduledJobDefinitionRepository = scheduledJobDefinitionRepository;
    }


    @PostConstruct
    @Override
    public void scheduleJobs() {
        Map<String, RunnableJob> jobBeans = context.getBeansOfType(RunnableJob.class);
        Map<String, RunnableJob> classNameToBean = jobBeans.values().stream()
                .collect(Collectors.toMap(job -> job.getClass().getSimpleName(), Function.identity()));

        List<ScheduledJobDefinition> jobs = findActiveScheduledJobs();

        for (ScheduledJobDefinition job : jobs) {
            String className = job.getJobName(); // e.g. "EmailReportJob"

            RunnableJob jobBean = classNameToBean.get(className);
            if (jobBean == null) {
                System.err.println("No matching job class found for: " + className);
                continue;
            }

            Runnable task = jobBean::run;
            CronTrigger trigger = new CronTrigger(job.getCronExpression());

            ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);
            scheduledTasks.put(className, future);
        }
    }

    @Override
    public void rescheduleJob(int id, String newCron) {
        ScheduledJobDefinition scheduledJob = findById(id);

        scheduledJob.setCronExpression(newCron);
        scheduledJob.setActive(true);
        save(scheduledJob);

        stopJob(scheduledJob.getJobName());

        RunnableJob jobBean = context.getBeansOfType(RunnableJob.class).values().stream()
                .filter(bean -> bean.getClass().getSimpleName().equals(scheduledJob.getJobName()))
                .findFirst()
                .orElse(null);

        if (jobBean == null) return;

        ScheduledFuture<?> future = taskScheduler.schedule(jobBean::run, new CronTrigger(newCron));
        scheduledTasks.put(scheduledJob.getJobName(), future);
    }

    @Override
    public Set<String> getActiveJobs() {
        return scheduledTasks.keySet();
    }

    @Override
    public ScheduledJobDefinition createJob(ScheduledJobDefinition scheduledJobDefinition, boolean isRegistered) {
        ScheduledJobDefinition scheduledJob = this.save(scheduledJobDefinition);

        if (isRegistered) {
            this.rescheduleJob(scheduledJob.getId(), scheduledJob.getCronExpression());
        }
        return scheduledJob;
    }

    private ScheduledJobDefinition save(ScheduledJobDefinition scheduledJobDefinition) {
        return scheduledJobDefinitionRepository.save(scheduledJobDefinition);
    }

    @Override
    public ScheduledJobDefinition findById(int id) {
        return scheduledJobDefinitionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public void deactivateJobById(int id) {
        scheduledJobDefinitionRepository.deactivateJobById(id);
        ScheduledJobDefinition scheduledJob = scheduledJobDefinitionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        stopJob(scheduledJob.getJobName());
    }

    @Override
    public void activateJobById(int id) {
        ScheduledJobDefinition scheduledJob = this.findById(id);
        this.rescheduleJob(scheduledJob.getId(), scheduledJob.getCronExpression());
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
}
