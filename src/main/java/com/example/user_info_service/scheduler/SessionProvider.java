package com.example.user_info_service.scheduler;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;
import javax.mail.Session;

public class SessionProvider {

    public static Session createSession(String host, int port, String emailUsername, String emailPassword,
                                        boolean startTlsRequired, boolean startTlsEnable, String socketFactoryClass, boolean debug) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.starttls.required", startTlsRequired);
        properties.put("mail.smtp.starttls.enable", startTlsEnable);
        properties.put("mail.smtp.socketFactory.class", socketFactoryClass);
        properties.put("mail.debug", debug);

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });
    }
}
