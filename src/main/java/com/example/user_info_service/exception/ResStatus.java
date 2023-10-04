package com.example.user_info_service.exception;

import lombok.Getter;

@Getter
public enum ResStatus {

    ENTER_NUMBER("5001", "please enter mobile number"),
    MOBILE_DIGIT("5002", "please enter 10 digit mobile number"),
    BOOKING_ID_NOT_FOUND("5003","BooingID not found"),
    INVALID_EMAIL("5004","Enter valid Email ID ");

    private final String code;
    private final String desc;
    ResStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

