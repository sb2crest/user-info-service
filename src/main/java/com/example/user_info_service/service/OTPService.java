package com.example.user_info_service.service;

import com.example.user_info_service.pojo.ValidateOTP;

public interface OTPService {
    String validateSMS(ValidateOTP validateOTP);

    String generateOTP(String mobile);
}
