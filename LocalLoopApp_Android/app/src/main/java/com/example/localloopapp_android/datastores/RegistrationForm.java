package com.example.localloopapp_android.datastores;

/**
 * A simple model class that stores trimmed and formatted user input
 * from the Create Account form.
 *
 * So we can pass it between methods instead of individual fields.
 */
public class RegistrationForm {

    // all fields are final -- to ensure immutability
    // and public -- because the purpose is to have access to them
    public final String firstName;
    public final String lastName;
    public final String username;
    public final String email;
    public final String phone;
    public final String password;
    public final String confirmPassword;
    public final boolean isOrganizer;
    public final String companyName;

    public RegistrationForm(String firstName, String lastName, String username, String email,
                            String phone, String password, String confirmPassword,
                            boolean isOrganizer, String companyName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.isOrganizer = isOrganizer;
        this.companyName = companyName;
    }
}