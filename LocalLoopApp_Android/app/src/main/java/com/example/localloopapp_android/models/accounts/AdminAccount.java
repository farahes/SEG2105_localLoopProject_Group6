package com.example.localloopapp_android.models.accounts;

/**
 * Models data for a person with admin privileges.
 * Used for Firebase serialization.
 * Not responsible for behavior or business logic.
 */

public class AdminAccount extends UserAccount {

    public AdminAccount() {
        super();
    }

    public AdminAccount(String userID, String firstName, String lastName, String username, String email, String phoneNumber, String role) {
        super(userID, firstName, lastName, username, email, phoneNumber, role);
    }
}