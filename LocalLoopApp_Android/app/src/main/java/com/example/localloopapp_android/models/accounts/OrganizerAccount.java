package com.example.localloopapp_android.models.accounts;

/**
 * Models data for an event organizer.
 * Stores profile fields used in authentication and event attribution.
 * Not responsible for behavior or business logic.
 */

public class OrganizerAccount extends UserAccount {
    private String companyName;

    public OrganizerAccount() {
        super();
    }

    public OrganizerAccount(String userID, String firstName, String lastName, String username,
                            String email, String phoneNumber, String role, String companyName) {
        super(userID, firstName, lastName, username, email, phoneNumber, role);
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
