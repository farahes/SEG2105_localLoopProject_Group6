package com.example.localloopapp_android.models;

public class Organizer extends User {
    protected String companyName;
    public Organizer() {
        super();
    }

    public Organizer(String userID, String firstName, String lastName, String username, String email, String phoneNumber, String role, String companyName) {
        super(userID, firstName, lastName, username, email, phoneNumber, role);
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return this.companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

}