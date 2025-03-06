package com.user.info.service.service;

import com.user.info.service.dto.OTPResponse;
import com.user.info.service.dto.ValidateOTP;
import org.springframework.http.ResponseEntity;

public interface OTPService {
    ResponseEntity<OTPResponse> validateSMS(ValidateOTP validateOTP);

    ResponseEntity<OTPResponse> generateOTP(String mobile);
}