package com.example.user_info_service.model;

import javax.mail.Message;
import javax.mail.MessagingException;

public interface EmailTransport {
    void send(Message message) throws MessagingException;
}