package com.example.user_info_service.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import javax.mail.*;
import java.util.Properties;

@PropertySource("classpath:application.properties")
public class DefaultEmailTransport implements EmailTransport {

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.username}")
    private String emailUsername;

    @Value("${spring.mail.password}")
    private String emailPassword;

    @Value("${spring.mail.port}")
    private int mailPort;

    private final String host = mailHost;
    private final int port = mailPort;  // Replace with the appropriate port number
    private final String username = emailUsername;
    private final String password = emailPassword;

    @Override
    public void send(Message message) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", mailHost);
        properties.put("mail.smtp.port", mailPort);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Transport.send(message);
    }

}
