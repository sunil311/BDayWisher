package com.bd.bwisher.quartz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang3.SystemUtils;
import org.apache.velocity.exception.VelocityException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.bd.bwisher.beans.BirthdayEmail;
import com.bd.bwisher.beans.config.ConfigBeans;
import com.bd.bwisher.helper.EmailHelper;
import com.bd.bwisher.service.EmailNotificationService;
import com.bd.bwisher.service.PivotService;
import com.bd.exceptions.BWisherException;

@Service
public class EmailSenderJob implements Job {

	static Logger logger = LoggerFactory.getLogger(EmailSenderJob.class);

	/**
	 * Execute actual logic of job, Here it iterating over all available
	 * employees for a specific day and send birthday mail to all of them.
	 * 
	 * @param context
	 */
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		ConfigBeans beans = new ConfigBeans();
		EmailNotificationService emailNotificationService = null;
		try {
			boolean mailSentForTheDay = checkMailStatus();
			mailSentForTheDay = false;
			if (!mailSentForTheDay) {
				emailNotificationService = new EmailNotificationService(
						beans.getMailSender(), beans.getVelocityEngine());
				PivotService pivotService = new PivotService();
				List<BirthdayEmail> employees = pivotService
						.getEmployees(new Date());
				for (BirthdayEmail birthdayEmail : employees) {
					if (birthdayEmail.getSUBJECT().equalsIgnoreCase("Birthday")) {
						emailNotificationService.sendEmail(EmailHelper
								.prepareBirthDayEmail(birthdayEmail));
					}
				}
				// Updating flag file
				updateFlagFile();
			}
		} catch (VelocityException | IOException | ParseException ex) {
			throw new BWisherException("Email sending failed.", ex);
		}
	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void updateFlagFile() throws FileNotFoundException, IOException {
		Properties p = new Properties();
		Date today = new Date();
		DateFormat formater = new SimpleDateFormat("MMMM d, yyyy",
				Locale.ENGLISH);
		p.setProperty("mail.sent.date", formater.format(today));
		p.store(new FileOutputStream(getFlagFile()), "Flag File Update for "
				+ formater.format(today));
	}

	/**
	 * @return
	 * @throws ParseException
	 */
	private boolean checkMailStatus() throws ParseException {
		File flagFile = getFlagFile();
		boolean mailSent = false;
		if (flagFile != null) {
			Properties p = new Properties();
			try {
				p.load(new FileInputStream(flagFile));
				DateFormat formater = new SimpleDateFormat("MMMM d, yyyy",
						Locale.ENGLISH);
				Date mailDate = formater.parse(p.getProperty("mail.sent.date"));
				Date today = new Date();
				if (today.getDate() == mailDate.getDate()) {
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
}
