package com.example.localloopapp_android.models.accounts;

/**
 * Represents a user participating in events.
 * Contains contact and identity information only.
 * Not responsible for behavior or business logic.
 */

public class ParticipantAccount extends UserAccount {
    public ParticipantAccount() {
        super();
    }

    public ParticipantAccount(String userID, String firstName, String lastName, String username, String email, String phoneNumber, String role) {
        super(userID, firstName, lastName, username, email, phoneNumber, role);
    }
}