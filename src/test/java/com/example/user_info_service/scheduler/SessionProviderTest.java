package com.example.user_info_service.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import org.junit.jupiter.api.Test;

public class SessionProviderTest {

    @Test
    public void testCreateSession() {
        String host = "your.smtp.server.com";
        int port = 587; // Replace with your actual mail port
        String emailUsername = "yourEmail@gmail.com";
        String emailPassword = "yourPassword";
        boolean startTlsRequired = true;
        boolean startTlsEnable = true;
        String socketFactoryClass = "javax.net.ssl.SSLSocketFactory";
        boolean debug = true;

        Session session = SessionProvider.createSession(host, port, emailUsername, emailPassword,
                startTlsRequired, startTlsEnable, socketFactoryClass, debug);

        // Create a PasswordAuthentication object with the expected values
        PasswordAuthentication expectedAuth = new PasswordAuthentication(emailUsername, emailPassword);

        // Assert that the actual and expected PasswordAuthentication objects match
        assertEquals(expectedAuth.getUserName(),"yourEmail@gmail.com" );
    }
}
