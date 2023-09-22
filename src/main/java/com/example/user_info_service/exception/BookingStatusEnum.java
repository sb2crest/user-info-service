package com.example.user_info_service.exception;

import lombok.Getter;

@Getter
public enum BookingStatusEnum {

    ENQUIRY("E","Enquiry"),
    CONFIRMED("C","Confirmed"),
    DECLINED("D","Declined");

    private final String code;
    private final String desc;
    BookingStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
