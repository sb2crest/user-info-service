package com.user.info.service.model;

import javax.mail.Message;
import javax.mail.MessagingException;

public interface EmailTransport {
    void send(Message message) throws MessagingException;
}