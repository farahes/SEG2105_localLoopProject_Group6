package com.example.localloopapp_android.models;

public class Participant extends User {
    public Participant() {
        super();
    }

    public Participant(String userID, String firstName, String lastName, String username, String email, String phoneNumber, String role) {
        super(userID, firstName, lastName, username, email, phoneNumber, role);
    }
}