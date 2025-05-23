package com.example.localloopapp_android;

import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-z -]{1,}$"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.length() >= 1; // Can adjust this line as needed for minimum username length
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8; // Can adjust this line as needed for minimum password length or enforce regex for password complexity
    }
}