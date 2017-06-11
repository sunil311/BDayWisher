package com.bd.bwisher.beans.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.velocity.VelocityEngineFactory;

import com.bd.bwisher.helper.BDayWisherProperties;

@Configuration
@SuppressWarnings("deprecation")
public class ConfigBeans {
    private Properties props = BDayWisherProperties.properties;

    /** Velocity configuration.
     * 
     * @return
     * @throws VelocityException
     * @throws IOException */
    @Bean
    public VelocityEngine getVelocityEngine() throws VelocityException, IOException {
        VelocityEngineFactory factory = new VelocityEngineFactory();
        Properties props = new Properties();
        props.put("resource.loader", "class");
        props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        factory.setVelocityProperties(props);
        return factory.createVelocityEngine();
    }

    /** JavaMail configuration.
     * 
     * @return */
    @Bean
    public JavaMailSender getMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setDefaultEncoding("UTF-8"); // Using mail from impetus.
        //mailSender.setHost("server-020.impetus.co.in");
        mailSender.setHost(props.getProperty("mail.smtp.host"));
        mailSender.setPort(Integer.parseInt(props.getProperty("mail.smtp.port")));
        mailSender.setUsername(props.getProperty("mail.smtp.username"));
        mailSender.setPassword(props.getProperty("mail.smtp.password"));

        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.auth", "true");
        javaMailProperties.put("mail.transport.protocol", "smtp"); 
        javaMailProperties.put("mail.debug", "true");
        javaMailProperties.put("mail.smtp.starttls.enable", "true");

        // Using gmail from gmail.
        /*
         * mailSender.setHost("smtp.gmail.com"); mailSender.setPort(587); mailSender.setUsername("sunil.mact@gmail.com");
         * mailSender.setPassword("sunil@311"); Properties javaMailProperties = new Properties(); javaMailProperties.put("mail.smtp.auth", "true");
         * javaMailProperties.put("mail.transport.protocol", "smtp"); javaMailProperties.put("mail.debug", "true");
         * javaMailProperties.put("mail.smtp.starttls.enable", "true");
         */
        mailSender.setJavaMailProperties(javaMailProperties);
        return mailSender;
    }
}
