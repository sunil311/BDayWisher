package com.bd;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.bd.bwisher.beans.config.QuartzProperties;
import com.bd.bwisher.helper.BDayWisherProperties;
import com.bd.bwisher.quartz.EmailSenderJob;
import com.bd.exceptions.BWisherException;

@SpringBootApplication
@ComponentScan(basePackages = { "com.bd" })
public class BirthDayWisher {

    // private static final Logger logger =
    // Logger.getLogger(BirthDayWisher.class);
    static Logger logger = LoggerFactory.getLogger(BirthDayWisher.class);

    public static void main(String[] args) {
        try {
            loadProperties();
            ConfigurableApplicationContext context = SpringApplication.run(BirthDayWisher.class, args);
            QuartzProperties quartzProperties = context.getBean(QuartzProperties.class);
            logger.debug("quartz properties" + quartzProperties);
            ScheduleJob(quartzProperties);
        } catch (Exception e) {
            throw new BWisherException(e);
        }
    }

    private static void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(BirthDayWisher.class.getClassLoader().getResourceAsStream("mail/smtp.properties"));
        properties.load(BirthDayWisher.class.getClassLoader().getResourceAsStream("mail/velocity.properties"));
        properties.load(BirthDayWisher.class.getClassLoader().getResourceAsStream("bday.properties"));
        properties.load(BirthDayWisher.class.getClassLoader().getResourceAsStream("bd.pivot.properties"));
        BDayWisherProperties.properties = properties;

    }

    /** Schedule Quartz job for running on a regular interval, quartz expression will be picked from bogdata.quartz.properties file placed in class
     * path.
     * 
     * @param quartzProperties
     * @throws SchedulerException
     * @throws InterruptedException */
    private static void ScheduleJob(QuartzProperties quartzProperties) throws SchedulerException, InterruptedException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler sched = schedulerFactory.getScheduler();
        JobDetail job = JobBuilder.newJob(EmailSenderJob.class).withIdentity(quartzProperties.getJobName(), quartzProperties.getGroupName()).build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(quartzProperties.getTriggerName(), quartzProperties.getGroupName())
                .withSchedule(CronScheduleBuilder.cronSchedule(quartzProperties.getCronExpression())).startAt(new Date()).build();

        sched.scheduleJob(job, trigger);
        sched.start();
        Thread.sleep(90L * 1000L);
    }
}
