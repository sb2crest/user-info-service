package com.example.user_info_service.exception;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SleeperType {
    FULL_SLEEPER("FS" , "Full-Sleeper"),
    SEMI_SLEEPER("SS" , "Semi-Sleeper"),
    NON_SLEEPER("NS" , "Non-Sleeper");

    private final String code;
    private final String desc;

    SleeperType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(String code) {
        return Arrays.stream(values()).filter(sleeperType -> sleeperType.code.equalsIgnoreCase(code)).findFirst()
                .map(sleeperType -> sleeperType.desc).orElse(code);
    }

    public static String getCodeByDesc(String desc) {
        return Arrays.stream(values()).filter(sleeperType -> sleeperType.desc.equalsIgnoreCase(desc)).findFirst()
                .map(sleeperType -> sleeperType.code).orElse(desc);
    }

}