package com.example.localloopapp_android.models;

public enum UserRole {
    ADMIN("admin"),
    ORGANIZER("organizer"),
    PARTICIPANT("participant");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromString(String role) {
        if (role == null) return null;
        for (UserRole r : values()) {
            if (r.value.equalsIgnoreCase(role)) {
                return r;
            }
        }
        return null;
    }
}
