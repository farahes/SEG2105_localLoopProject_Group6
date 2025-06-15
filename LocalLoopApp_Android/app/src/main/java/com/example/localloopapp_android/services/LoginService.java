package com.example.localloopapp_android.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.localloopapp_android.models.UserRole;
import com.example.localloopapp_android.models.accounts.AdminAccount;
import com.example.localloopapp_android.models.accounts.OrganizerAccount;
import com.example.localloopapp_android.models.accounts.ParticipantAccount;
import com.example.localloopapp_android.models.accounts.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

/**
 * Handles all login-related Firebase logic, including email/username lookup and role-based parsing.
 * Pure service — no activity or UI logic.
 */
public class LoginService {

    private static final String TAG = "LoginService";

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    /**
     * Attempts to log in the user using either their email or username.
     * If the identifier is an email, it uses Firebase Authentication to sign in.
     * If it's a username, it looks up the email in Firebase Realtime Database and then signs in.
     *
     * @param identifier The user's email or username.
     * @param password   The user's password.
     * @param context    The Android context (not used here, but can be useful for callbacks).
     * @param callback   Callback to handle success or failure of the login attempt.
     */
    public void login(String identifier, String password, android.content.Context context, LoginCallback callback) {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            signInWithEmail(identifier, password, callback);
        } else {
            lookupEmailByUsername(identifier.toLowerCase(), password, callback);
        }
    }

    /**
     * Looks up the user's email by their username in Firebase Realtime Database.
     * If found, attempts to sign in with that email and the provided password.
     *
     * @param username The username to look up.
     * @param password The password for login.
     * @param callback Callback to handle success or failure.
     */
    private void lookupEmailByUsername(String username, String password, LoginCallback callback) {
        Query query = usersRef.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("Login failed: Invalid username or password.");
                    return;
                }

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    UserAccount user = parseUserSnapshot(userSnapshot);
                    if (user == null) continue;

                    if (user.getStatusEnum() != UserAccount.Status.ACTIVE) {
                        callback.onFailure("This account is currently disabled. Please contact support.");
                        return;
                    }

                    if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                        signInWithEmail(user.getEmail(), password, callback);
                        return;
                    }
                }

                callback.onFailure("Could not retrieve valid user. Please check your credentials.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Username query cancelled", error.toException());
                callback.onFailure("Database error: " + error.getMessage());
            }
        });
    }

    /**
     * Signs in the user with email and password using Firebase Authentication.
     * If successful, fetches the user profile and returns it via callback.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param callback Callback to handle success or failure.
     */
    private void signInWithEmail(String email, String password, LoginCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        callback.onFailure("Login failed: Invalid credentials.");
                        return;
                    }

                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser == null) {
                        callback.onFailure("Login error: No user instance returned.");
                        return;
                    }

                    fetchUserProfile(firebaseUser.getUid(), callback);
                });
    }

    /**
     * Fetches the user profile from Firebase based on the user ID.
     * Parses the user data into a UserAccount object based on their role.
     *
     * @param userId   The Firebase user ID to look up.
     * @param callback Callback to handle success or failure.
     */
    private void fetchUserProfile(String userId, LoginCallback callback) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("User profile not found.");
                    return;
                }

                UserAccount user = parseUserSnapshot(snapshot);
                if (user == null) {
                    callback.onFailure("Failed to parse user profile.");
                    return;
                }

                if (user.getUserID() == null) {
                    user.setUserID(userId);
                }

                callback.onSuccess(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "fetchUserProfile:onCancelled", error.toException());
                callback.onFailure("Failed to load user data: " + error.getMessage());
            }
        });
    }

    /**
     * Parses a Firebase DataSnapshot into a UserAccount object based on the user's role.
     *
     * @param snapshot The DataSnapshot containing user data from Firebase.
     * @return A UserAccount instance of the appropriate subclass (AdminAccount, OrganizerAccount, or ParticipantAccount),
     *         or null if the role is unknown or parsing fails.
     */
    private UserAccount parseUserSnapshot(DataSnapshot snapshot) {
        UserRole role = UserRole.fromString(snapshot.child("role").getValue(String.class));
        switch (role) {
            case ADMIN:
                return snapshot.getValue(AdminAccount.class);
            case ORGANIZER:
                return snapshot.getValue(OrganizerAccount.class);
            case PARTICIPANT:
                return snapshot.getValue(ParticipantAccount.class);
            default:
                Log.e(TAG, "Unknown role in snapshot: " + role);
                return null;
        }
    }

    /**
     * Interface for handling login result.
     */
    public interface LoginCallback {
        void onSuccess(UserAccount user);
        void onFailure(String errorMessage);
    }
}
