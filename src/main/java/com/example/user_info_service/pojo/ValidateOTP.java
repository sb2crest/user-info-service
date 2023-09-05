package com.example.user_info_service.pojo;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ValidateOTP {

    private String mobile;

    private String otp;
}
