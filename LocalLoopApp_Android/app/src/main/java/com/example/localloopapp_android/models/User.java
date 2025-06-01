package com.example.localloopapp_android.models;

import com.google.firebase.database.Exclude;

public abstract class User {
    protected String userID;
    protected String firstName;
    protected String lastName;
    protected String username;
    protected String email;
    protected String phoneNumber;
    protected String role;

    public User() {}

    public User(String userID, String firstName, String lastName, String username, String email, String phoneNumber, String role) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public String getUserID() {
        return this.userID;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }
    public String getUsername() {
        return this.username;
    }
    public String getEmail() {
        return this.email;
    }

    public String getPhoneNumber() { return this.phoneNumber; }
    public String getRole() {
        return this.role;
    }

    public void setUserID(String id){ this.userID = id; }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setRole(String role) {
        this.role = role;
    }

    @Exclude
    public String getWelcomeMessage() {
        return "Welcome " + this.getFirstName() + "! You are logged in as " + this.getRole() + ".";
    }

    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}