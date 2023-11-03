package com.example.user_info_service.controller;

import com.example.user_info_service.dto.ValidateOTP;
import com.example.user_info_service.service.OTPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@CrossOrigin(origins = "http://localhost:8100")
public class OTPController {
    private final OTPService otpService;

    @Autowired
    public OTPController(OTPService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/sendOTP")
    String generateOTP(@RequestParam String mobile){
        return otpService.generateOTP(mobile);
    }
    @PostMapping("/validateOTP")
    String validateSMS(@RequestBody ValidateOTP validateOTP) {
        return otpService.validateSMS(validateOTP);
    }
}


