package com.example.user_info_service.config;

import com.example.user_info_service.model.DefaultEmailTransport;
import com.example.user_info_service.model.EmailTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfiguration {
    @Bean
    public EmailTransport defaultEmailTransport() {
        
        return new DefaultEmailTransport();
    }
}