package com.example.user_info_service.exception;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ACType {
    AC("AC", "AC"),
    NON_AC("NA", "Non AC");

    private final String code;
    private final String desc;

    ACType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(String code) {
        return Arrays.stream(values()).filter(acType -> acType.code.equalsIgnoreCase(code)).findFirst()
                .map(acType -> acType.desc).orElse(code);
    }

    public static String getCodeByDesc(String desc) {
        return Arrays.stream(values()).filter(acType -> acType.desc.equalsIgnoreCase(desc)).findFirst()
                .map(acType -> acType.code).orElse(desc);
    }
}