package com.user.info.service.controller;

import com.user.info.service.dto.OTPResponse;
import com.user.info.service.dto.ValidateOTP;
import com.user.info.service.service.OTPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@CrossOrigin(origins = {"http://localhost:8100","http://nandubus.in"})
public class OTPController {
    private final OTPService otpService;

    @Autowired
    public OTPController(OTPService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/sendOTP")
    ResponseEntity<OTPResponse> generateOTP(@RequestParam String mobile){
        return otpService.generateOTP(mobile);
    }
    @PostMapping("/validateOTP")
    ResponseEntity<OTPResponse> validateSMS(@RequestBody ValidateOTP validateOTP) {
        return otpService.validateSMS(validateOTP);
    }
}