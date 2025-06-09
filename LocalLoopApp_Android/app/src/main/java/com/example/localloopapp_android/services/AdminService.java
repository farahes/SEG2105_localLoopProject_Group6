package com.example.localloopapp_android.services;

import android.util.Log;

import com.example.localloopapp_android.models.accounts.UserAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * AdminService
 *
 * Handles admin-specific backend operations:
 * - Fetching user list
 * - Toggling user active/inactive status
 * - Deleting users
 *
 * Called by AdminDashboardActivity. Acts as a bridge to Firebase,
 * keeping business logic out of the UI.
 */
public class AdminService {

    private final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    /**
     * Represents a single user row from Firebase with its key and parsed user.
     */
    public static class UserRow {
        public final String firebaseKey;
        public final UserAccount user;

        public UserRow(String firebaseKey, UserAccount user) {
            this.firebaseKey = firebaseKey;
            this.user = user;
        }
    }

    /**
     * Fetches all users from the Firebase database.
     */
    public void getAllUsers(Consumer<List<UserRow>> onSuccess, Consumer<DatabaseError> onError) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<UserRow> result = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    UserAccount user = parseUser(userSnapshot);
                    result.add(new UserRow(userSnapshot.getKey(), user));
                }
                onSuccess.accept(result);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                onError.accept(error);
            }
        });
    }

    /**
     * Toggles a user's active/inactive status.
     */
    public void toggleUserStatus(String firebaseKey, UserAccount.Status currentStatus, Runnable onComplete) {
        UserAccount.Status newStatus = (currentStatus == UserAccount.Status.ACTIVE)
                ? UserAccount.Status.INACTIVE
                : UserAccount.Status.ACTIVE;

        usersRef.child(firebaseKey).child("status").setValue(newStatus.name())
                .addOnSuccessListener(unused -> onComplete.run());
    }

    /**
     * Deletes a user from Firebase.
     */
    public void deleteUser(String firebaseKey, Runnable onComplete) {
        usersRef.child(firebaseKey).removeValue()
                .addOnSuccessListener(unused -> onComplete.run())
                .addOnFailureListener(e -> Log.e("AdminService", "deleteUser: " + e.getMessage(), e));
    }

    /**
     * Converts a Firebase snapshot into a UserAccount.
     */
    private UserAccount parseUser(DataSnapshot snapshot) {
        UserAccount user = new UserAccount() {};
        user.setUserID(snapshot.child("userID").getValue(String.class));
        user.setFirstName(snapshot.child("firstName").getValue(String.class));
        user.setLastName(snapshot.child("lastName").getValue(String.class));
        user.setUsername(snapshot.child("username").getValue(String.class));
        user.setEmail(snapshot.child("email").getValue(String.class));
        user.setPhoneNumber(snapshot.child("phoneNumber").getValue(String.class));
        user.setRole(snapshot.child("role").getValue(String.class));
        user.setStatus(snapshot.child("status").getValue(String.class));
        return user;
    }
}
