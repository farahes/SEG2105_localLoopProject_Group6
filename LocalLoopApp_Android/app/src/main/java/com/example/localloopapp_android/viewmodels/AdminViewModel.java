package com.example.localloopapp_android.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.localloopapp_android.models.accounts.UserAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for admin operations: managing users.
 * Exposes LiveData so the UI can react to changes automatically.
 */
public class AdminViewModel extends ViewModel {

    private final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    private final MutableLiveData<List<UserRow>> userList = new MutableLiveData<>();

    public LiveData<List<UserRow>> getUserList() {
        return userList;
    }

    public static class UserRow {
        public final String firebaseKey;
        public final UserAccount user;

        public UserRow(String firebaseKey, UserAccount user) {
            this.firebaseKey = firebaseKey;
            this.user = user;
        }
    }

    /**
     * Loads users into LiveData.
     */
    public void fetchAllUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<UserRow> result = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    UserAccount user = parseUser(userSnapshot);
                    result.add(new UserRow(userSnapshot.getKey(), user));
                }
                userList.setValue(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminViewModel", "Error loading users", error.toException());
            }
        });
    }

    /**
     * Toggles a user's status and refreshes the list.
     */
    public void toggleUserStatus(String firebaseKey, UserAccount.Status currentStatus) {
        UserAccount.Status newStatus = (currentStatus == UserAccount.Status.ACTIVE)
                ? UserAccount.Status.INACTIVE
                : UserAccount.Status.ACTIVE;

        usersRef.child(firebaseKey).child("status").setValue(newStatus.name())
                .addOnSuccessListener(unused -> fetchAllUsers());
    }

    /**
     * Deletes a user and refreshes the list.
     */
    public void deleteUser(String firebaseKey) {
        usersRef.child(firebaseKey).removeValue()
                .addOnSuccessListener(unused -> fetchAllUsers())
                .addOnFailureListener(e -> Log.e("AdminViewModel", "deleteUser: " + e.getMessage(), e));
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
