package com.example.localloopapp_android.models;

public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(String userID, String firstName, String lastName, String username, String email, String phoneNumber, String role) {
        super(userID, firstName, lastName, username, email, phoneNumber, role);
    }
}