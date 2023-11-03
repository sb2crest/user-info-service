package com.example.user_info_service.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BookingStatusEnum {

    ENQUIRY("E","Enquiry"),
    BOOKED("B","Booked"),
    DECLINED("D","Declined"),
    COMPLETED("C","Completed");

    private final String code;
    private final String desc;
    BookingStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public static String getDesc(String code){
        return Arrays.stream(BookingStatusEnum.values()).filter(v -> v.code.equalsIgnoreCase(code)).findFirst()
                .map(BookingStatusEnum::getDesc).orElse(code);
    }
}
