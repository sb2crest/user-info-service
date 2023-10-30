package com.example.user_info_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

@RestController
public class WhatsAppController {

    public static final String ACCOUNT_SID = "AC9b56e912837e7b190d98343ae02a2ec9";
    public static final String AUTH_TOKEN = "0e9355e326bd143a248411c2952d0c3c";

    @GetMapping("/whatsapp")
    public void sendMessage() {

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber("whatsapp:+919535858675"),
                new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
                "Your appointment is coming up on July 21 at 3PM"

        ).create();

        System.out.println(message.getSid());
    }

}
