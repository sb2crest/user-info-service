package com.example.user_info_service.config;

import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.razorpay.RazorpayClient;

@Configuration
public class RazorpayConfig {

    @Value("${razorpay.api.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.api.key.secret}")
    private String razorpayKeySecret;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }
}
