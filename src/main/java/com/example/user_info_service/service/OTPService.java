package com.example.user_info_service.service;

import com.example.user_info_service.dto.OTPResponse;
import com.example.user_info_service.dto.ValidateOTP;
import org.springframework.http.ResponseEntity;

public interface OTPService {
    String validateSMS(ValidateOTP validateOTP);

    ResponseEntity<OTPResponse> generateOTP(String mobile);
}