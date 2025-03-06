package com.user.info.service.config;

import com.user.info.service.model.DefaultEmailTransport;
import com.user.info.service.model.EmailTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfiguration {
    @Bean
    public EmailTransport defaultEmailTransport() {
        
        return new DefaultEmailTransport();
    }
}