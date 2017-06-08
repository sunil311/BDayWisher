package com.bd.bwisher.helper;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.bd.bwisher.beans.BirthdayEmail;
import com.bd.bwisher.quartz.EmailSenderJob;
import com.bd.exceptions.BWisherException;

@SuppressWarnings("deprecation")
public class EmailHelper {

	static Logger logger = LoggerFactory.getLogger(EmailSenderJob.class);

	/**
	 * Preparing MessagePreparator: includes all required content for sending
	 * mail.
	 * 
	 * @param velocityEngine
	 * @param birthdayEmail
	 * @return
	 */
	public static MimeMessagePreparator getMessagePreparator(
			VelocityEngine velocityEngine, final BirthdayEmail birthdayEmail) {

		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper messageHelper = new MimeMessageHelper(
						mimeMessage, MimeMessageHelper.MULTIPART_MODE_RELATED,
						"UTF-8");
				
				
				
				messageHelper.setSubject(birthdayEmail.getSUBJECT());
				messageHelper.setFrom("sparkbd@impetus.co.in");
				messageHelper.setTo(birthdayEmail.getEMAIL());
				Map<String, Object> model = new HashMap<>();
				model.put("name", birthdayEmail.getNAME());
				model.put("picUrl", birthdayEmail.getIMGURL());
				model.put("host", InetAddress.getLocalHost().getHostAddress());
				model.put("port", "7777");
				messageHelper.setText(
						geVelocityTemplateContent(velocityEngine, model,
								birthdayEmail.getTemplateName()), true);
				//messageHelper.addInline("background_image", new File("/home/impadmin/git/BDayWisher/src/main/resources/public/template-1.png"));
			}
		};
		return preparator;
	}

	/**
	 * Evaluating velocity template content, executing with velocity engine.
	 * 
	 * @param velocityEngine
	 * @param model
	 * @param templateName
	 * @return
	 */
	public static String geVelocityTemplateContent(
			VelocityEngine velocityEngine, Map<String, Object> model,
			String templateName) {
		StringBuffer content = new StringBuffer();
		try {
			content.append(VelocityEngineUtils.mergeTemplateIntoString(
					velocityEngine, templateName, model));
			return content.toString();
		} catch (BWisherException e) {
			throw new BWisherException(
					"Unable to parse velocity template contant.", e);
		}
	}

	/**
	 * Populating BirthdayEmail object for a particular employee from JsonNode
	 * received from pivot service.
	 * 
	 * @param employee
	 * @return
	 */
	public static BirthdayEmail prepareBirthDayEmail(BirthdayEmail employee) {
		String template = selectTemplateName();
		logger.debug("template name :" + template);
		employee.setTemplateName(template);
		return employee;
	}

	/**
	 * This method is choosing random email template from available list of
	 * templates. we are expecting templates will be place inside
	 * /src/main/resources/main/templates folder.
	 * 
	 * @return
	 */
	private static String selectTemplateName() {
		String templatePath = "mail/templates";
		File file = new File(EmailHelper.class.getClassLoader()
				.getResource(templatePath).getFile());
		File[] templates = null;
		String template = "";
		if (file.isDirectory()) {
			templates = file.listFiles();
			int index = RandomUtils.nextInt(templates.length - 1);
			if (templates[index].isFile()) {
				template = "/" + templatePath + "/"
						+ templates[index].getName();
			} else {
				template = "/mail/templates/default/default.vm";
			}
		} else {
			template = "/mail/templates/default/default.vm";
		}
		return template;
	}
}
