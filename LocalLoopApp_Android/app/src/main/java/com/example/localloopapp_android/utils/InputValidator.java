package com.example.localloopapp_android.utils;

import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-z -]{1,}$"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\d{9,10}$"
    );

    public static boolean isValidName(String name) {
        return name == null || !NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return false;
        String cleaned = phoneNumber.replaceAll("[-\\s]", ""); // removes dashes and spaces
        return PHONE_PATTERN.matcher(cleaned).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && !username.isEmpty() && username.matches("^[a-zA-Z0-9]+$") &&
                3 <= username.length() && username.length() <= 20;
    }

    public static boolean isValidPassword(String password) {
        return password != null && 8 <= password.length() && password.length() <= 16;
    }
}