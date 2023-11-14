package com.example.user_info_service.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GmailValidator {
    public static boolean isValidGmail(String email) {
        String regex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+.[a-zA-Z0-9.-]$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}