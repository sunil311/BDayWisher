package com.bd.bwisher.service;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import com.bd.bwisher.beans.BirthdayEmail;
import com.bd.bwisher.helper.EmailHelper;
import com.bd.exceptions.BWisherException;

@Service("mailService")
public class EmailNotificationService {

	private static final Logger logger = Logger
			.getLogger(EmailNotificationService.class);

	JavaMailSender mailSender;

	VelocityEngine velocityEngine;

	public EmailNotificationService(JavaMailSender mailSender,
			VelocityEngine velocityEngine) {
		this.mailSender = mailSender;
		this.velocityEngine = velocityEngine;
	}

	/**
	 * Sending mail to employee.
	 * 
	 * @param birthdayEmail
	 */
	public void sendEmail(BirthdayEmail birthdayEmail) {

		logger.debug("birthday details " + birthdayEmail.toString());
		MimeMessagePreparator preparator = EmailHelper.getMessagePreparator(
				velocityEngine, birthdayEmail);
		try {
			mailSender.send(preparator);
		} catch (MailException ex) {
			throw new BWisherException("Email Sending failed.", ex);
		}
	}
}