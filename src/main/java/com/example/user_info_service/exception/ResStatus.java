package com.example.user_info_service.exception;

import lombok.Getter;

@Getter
public enum ResStatus {

    ENTER_NUMBER("5001", "please enter mobile number"),
    MOBILE_DIGIT("5002", "please enter 10 digit mobile number"),
    BOOKING_NOT_FOUND("5003","Booking Details not found"),
    VEHICLE_NOT_FOUND("5004","Vehicle Details not found"),
    USER_NOT_FOUND("5005","User Details not found"),
    SLOTS_NOT_FOUND("5006","Slots Details not found"),
    INVALID_EMAIL("5007","Invalid Email ID "),
    NO_RECORD_FOUND_WITH_ID_AND_MOBILE("5008","No record found with matching ID and Mobile Number "),
    BOOKING_DATA_NOT_FOUND_WITH_MOBILE("5009","Booking Details not found for this Mobile Number"),;

    private final String code;
    private final String desc;
    ResStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

