package com.user.info.service.model;

import java.util.regex.*;

public class VRNValidation {
    private VRNValidation(){
    }
    public static boolean isValid(String number){
        String regexp = "(([a-zA-Z,]){2}([\\d,]{0,4})([a-zA-Z]{0,3})([\\d,]){4})$";

        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher=pattern.matcher(number);
        return matcher.matches();
    }
}
