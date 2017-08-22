package job;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import util.Configures;

/**
 * Created by neshati on 2/7/2017.
 * Behpardaz
 */
public class Scheduler {
    //private static String EVERY30SECONDS = "0/20 * * * * ? *";
    private org.quartz.Scheduler scheduler;

    public Scheduler() {
        try {
            this.scheduler = new StdSchedulerFactory().getScheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void addJobs(){
        try {
            registerJob(NotifyScoreJob.class, "notify_score_service", "group1", Configures.score_notifier_service_runPattern);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws SchedulerException {
        Scheduler scheduler = new Scheduler();
        Configures.setConfigs();
        scheduler.start();
        scheduler.addJobs();
    }

    private void registerJob(Class job_class, String job_name, String job_group, String schedule_pattern) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(job_class).withIdentity(job_name, job_group).build();
        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity(job_name, job_group)
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(schedule_pattern))
                .build();
        scheduler.scheduleJob(job, trigger);
    }
}