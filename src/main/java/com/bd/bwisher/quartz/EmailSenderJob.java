package com.bd.bwisher.quartz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.SystemUtils;
import org.apache.velocity.exception.VelocityException;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.bd.bwisher.beans.BirthdayEmail;
import com.bd.bwisher.beans.config.ConfigBeans;
import com.bd.bwisher.helper.BDayWisherProperties;
import com.bd.bwisher.helper.EmailHelper;
import com.bd.bwisher.service.EmailNotificationService;
import com.bd.bwisher.service.PivotService;
import com.bd.exceptions.BWisherException;

@Service
public class EmailSenderJob implements Job {

    static Logger logger = LoggerFactory.getLogger(EmailSenderJob.class);
    private Properties props = BDayWisherProperties.properties;

    /** Execute actual logic of job, Here it iterating over all available employees for a specific day and send birthday mail to all of them.
     * 
     * @param context */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        ConfigBeans beans = new ConfigBeans();
        EmailNotificationService emailNotificationService = null;
        try {

            // ##################
            int mailCounter = getMailCounter();
            for (int i = mailCounter - 1; i >= 0; i--) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -i);
                Date mailDate = c.getTime();
                if (!checkMailStatus(mailDate)) {
                    emailNotificationService = new EmailNotificationService(beans.getMailSender(), beans.getVelocityEngine());
                    PivotService pivotService = new PivotService();
                    List<BirthdayEmail> employees = pivotService.getEmployees(mailDate);
                    for (BirthdayEmail birthdayEmail : employees) {
                        if (birthdayEmail.getSUBJECT().equalsIgnoreCase("Birthday")) {
                            if (i != 0) {
                                birthdayEmail.setSUBJECT("Belated " + birthdayEmail.getSUBJECT());
                            } else {
                                birthdayEmail.setSUBJECT(birthdayEmail.getSUBJECT());
                            }
                            emailNotificationService.sendEmail(EmailHelper.prepareBirthDayEmail(birthdayEmail));
                        }
                    }
                    // Updating flag file
                    updateFlagFile(mailDate);
                }
            }
            // ##################
        } catch (VelocityException | IOException | ParseException ex) {
            throw new BWisherException("Email sending failed.", ex);
        }
    }

    /** @return
     * @throws ParseException */
    private int getMailCounter() throws ParseException {
        Date lastSentDate = getLastSentDate();
        if (lastSentDate == null) {
            return 1;
        }
        DateTime dtLast = new DateTime(lastSentDate.getTime());
        int mailCounter = Days.daysBetween(dtLast.toLocalDate(), new DateTime().toLocalDate()).getDays();

        // If month is different

        if (props.getProperty("previous.mail.sent.day") != null) {
            int previousMailsDay = Integer.valueOf(props.getProperty("previous.mail.sent.day"));
            if (mailCounter > previousMailsDay) {
                mailCounter = previousMailsDay;
            }
        }
        return mailCounter;
    }

    /** @return
     * @throws ParseException */
    private Date getLastSentDate() throws ParseException {
        File flagFile = getFlagFile();
        if (flagFile != null) {
            Properties p = new Properties();
            try {
                p.load(new FileInputStream(flagFile));
                DateFormat formater = new SimpleDateFormat("MM/dd/yyyy");
                Date mailDate = formater.parse(p.getProperty("mail.sent.date"));
                return mailDate;
            } catch (FileNotFoundException e) {
                logger.info("Flag file does not exists.");
            } catch (IOException e) {
                logger.info("Unable to read flag file");
            }
        }
        return null;
    }

    /** @throws FileNotFoundException
     * @throws IOException */
    private void updateFlagFile(Date mailDate) throws FileNotFoundException, IOException {
        Properties p = new Properties();
        DateFormat formater = new SimpleDateFormat("MM/dd/yyyy");
        p.setProperty("mail.sent.date", formater.format(mailDate));
        p.store(new FileOutputStream(getFlagFile()), "Flag File Update for " + formater.format(mailDate));
    }

    /** @return */
    private File getFlagFile() {
        String flagFile = null;
        if (SystemUtils.IS_OS_LINUX) {
            flagFile = "/usr/local/bwisher.flag";
        } else if (SystemUtils.IS_OS_WINDOWS) {
            flagFile = "c:\bwisher.flag";
        }
        File file = new File(flagFile);
        return file;
    }

    /** @param mailDate2
     * @return
     * @throws ParseException */
    private boolean checkMailStatus(Date mailDate) throws ParseException {
        File flagFile = getFlagFile();
        boolean mailSent = false;
        if (flagFile != null) {
            Properties p = new Properties();
            try {
                p.load(new FileInputStream(flagFile));
                DateFormat formater = new SimpleDateFormat("MM/dd/yyyy");
                Date lastMailDate = formater.parse(p.getProperty("mail.sent.date"));
                if (mailDate.getDate() == lastMailDate.getDate()) {
                    mailSent = true;
                }
            } catch (FileNotFoundException e) {
                logger.info("Flag file does not exists.");
            } catch (IOException e) {
                logger.info("Unable to read flag file");
            }
        }
        return mailSent;
    }
}
