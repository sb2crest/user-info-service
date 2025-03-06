package com.user.info.service.exception;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SleeperType {
    SLEEPER("FS" , "Sleeper"),
    SEMI_SLEEPER("SS" , "Semi-Sleeper"),
    SEATER("NS" , "Seater");

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
