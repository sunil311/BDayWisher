package com.bd.bwisher.beans.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("bday.properties")
public class BDayConfiguration {

	@Value("${directorypath}")
	String bDataFileName;

	@Value("${exception.reciever}")
	String mailReviever;

	public String getbDataFileName() {
		return bDataFileName;
	}

	public void setbDataFileName(String bDataFileName) {
		this.bDataFileName = bDataFileName;
	}

	public String getMailReviever() {
		return mailReviever;
	}

	public void setMailReviever(String mailReviever) {
		this.mailReviever = mailReviever;
	}

}