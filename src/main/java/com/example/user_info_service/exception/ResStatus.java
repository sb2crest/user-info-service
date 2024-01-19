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
    BOOKING_DATA_NOT_FOUND_WITH_MOBILE("5009","Booking Details not found for this Mobile Number"),
    INVALID_NUMBER("4001", "invalid vehicle number"),
    DUPLICATE_NUMBER("4002", "vehicle number already exist"),
    VEHICLE_NUMBER("4003", "please enter vehicle number"),
    PAYMENT_DETAILS_NOT_FOUND("5010","Payment Details not found"),
    VEHICLE_NOT_AVAILABLE("5011","Vehicle Not Available for the date specified"),
    DISTANCE_CALCULATION("5012","Error occurs during calculating the distance"),
    DISTANCE_CALCULATION_URL("5013","Error occurs while hitting the URL for distance calculation"),
    ERROR_WHILE_SENDING_EMAIL("5014","Error occurs while sending an email"),
    ERROR_WHILE_READING_EMAIL_PATH("5015","Error occurs while reading an email path"),
    ;

    private final String code;
    private final String desc;
    ResStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

