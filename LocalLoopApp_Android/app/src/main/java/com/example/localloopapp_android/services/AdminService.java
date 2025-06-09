package com.example.localloopapp_android.services;

/**
 * Handles admin-specific operations like disabling or deleting user accounts.
 * Called by AdminDashboardActivity. Does not know about UI.
 */
public class AdminService {

    /**
     * Disables a user by setting their status to INACTIVE in the DB.
     * @param userId Firebase UID of the user to disable
     */
    public void disableUser(String userId) {
        // TODO: Update user's status in Firebase
    }

    /**
     * Deletes a user account from the database.
     * @param userId Firebase UID of the user to delete
     */
    public void deleteUser(String userId) {
        // TODO: Remove user entry from Firebase DB
    }
}

