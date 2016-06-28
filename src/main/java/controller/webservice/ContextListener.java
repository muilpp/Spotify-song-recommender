package controller.webservice;

import static model.Constant.EXECUTE_USER_JOB;
import static model.Constant.EXECUTE_USER_TRIGGER;
import static model.Constant.JOB_MANAGER_GROUP;
import static model.Constant.USER_SCHEDULE;
import static org.quartz.CronScheduleBuilder.weeklyOnDayAndHourAndMinute;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import model.UserSongsJob;

public class ContextListener implements ServletContextListener {
    private final static Logger LOGGER = Logger.getLogger(ContextListener.class.getName());
    private Scheduler schedule;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        try {
            SchedulerFactory schedFact = new StdSchedulerFactory();
            schedule = schedFact.getScheduler();
            schedule.start();

            // define the job and tie it to our class
            JobDetail executeAlertJob = createJob(UserSongsJob.class, EXECUTE_USER_JOB, JOB_MANAGER_GROUP);
            Trigger executeAlertTrigger = createOnceAWeekTrigger(EXECUTE_USER_TRIGGER, JOB_MANAGER_GROUP);

            // Tell quartz to schedule the job using our trigger
            schedule.scheduleJob(executeAlertJob, executeAlertTrigger);

            servletContext.setAttribute (USER_SCHEDULE, schedule);  
        } catch (SchedulerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private <T extends Job> JobDetail createJob(Class<T> jobClass, final String flightManagerJob, final String flightManagerGroup) {
        JobDetail jobDetail = newJob(jobClass)
                .withIdentity(flightManagerJob, flightManagerGroup)
                .build();

        return jobDetail;
    }

    private Trigger createOnceAWeekTrigger(final String flightTriggerManager, final String flightManagerGroup) {
        Trigger trigger = newTrigger()
                .withIdentity(flightTriggerManager, flightManagerGroup)
                .startNow()
                .withSchedule(weeklyOnDayAndHourAndMinute(DateBuilder.MONDAY, 0, 0))
                .build();

        return trigger;
    }

    /**
    * @see ServletContextListener#contextDestroyed(ServletContextEvent)
    */
    public void contextDestroyed(ServletContextEvent arg0) {
        ServletContext servletContext = arg0.getServletContext();

        // cancel all pending tasks in the timers queue
        if (schedule != null)
            try {
                /* Shutdown must have a boolean true argument, if not it will wait for quartz to finish and in the long run it will produce memory leaks */
                schedule.shutdown(true);
            } catch (SchedulerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }

        // remove the timer from the servlet context
        servletContext.removeAttribute(USER_SCHEDULE);  
    }
}