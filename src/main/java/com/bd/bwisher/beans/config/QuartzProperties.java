package com.bd.bwisher.beans.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:bd.quartz.properties")
public class QuartzProperties {

	@Value("${quartz.enabled}")
	String isEnabled;

	@Value("${quartz.cron}")
	String cronExpression;

	@Value("${quartz.jobName}")
	String jobName;

	@Value("${quartz.groupName}")
	String groupName;

	@Value("${quartz.triggerName")
	String triggerName;

	public String getIsEnabled() {
		return isEnabled;
	}

	public void setIsEnabled(String isEnabled) {
		this.isEnabled = isEnabled;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Override
	public String toString() {
		return "QuartzProperties [isEnabled=" + isEnabled + ", cronExpression="
				+ cronExpression + "]";
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

}
