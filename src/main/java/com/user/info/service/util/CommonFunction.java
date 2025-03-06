package com.user.info.service.util;

import com.user.info.service.exception.ACType;
import com.user.info.service.exception.SleeperType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonFunction {

    private static final String COMMA = ",";
    private static final String SLASH = "/";
    private static final String ALL = "ALL";

    public static String[] splitUsingSlash(String filter) {
        return filter.split(SLASH);
    }

    public static String[] splitUsingComma(String filter) {
        return filter.split(COMMA);
    }

    public static List<String> getFilterDetails(String filterData) {
        String[] filter = splitUsingSlash(filterData);
        String acDetail = filter[0];
        String sleeperDetail = filter[1];

        List<String> sleeperDetails = sleeperDetail.equalsIgnoreCase(ALL)
                ? Arrays.asList(SleeperType.SLEEPER.getCode(), SleeperType.SEMI_SLEEPER.getCode(), SleeperType.SEATER.getCode())
                : Arrays.asList(splitUsingComma(sleeperDetail));

        return getList(acDetail, sleeperDetails);
    }

    @NotNull
    private static List<String> getList(String acDetail, List<String> sleeperDetails) {
        List<String> acDetails = acDetail.equalsIgnoreCase(ALL)
                ? Arrays.asList(ACType.AC.getCode(), ACType.NON_AC.getCode())
                : Arrays.asList(splitUsingComma(acDetail));

        List<String> filters = new ArrayList<>();

        for (String ac : acDetails) {
            for (String sleeper : sleeperDetails) {
                filters.add(ac.toUpperCase() + SLASH + sleeper.toUpperCase());
            }
        }
        return filters;
    }
}
