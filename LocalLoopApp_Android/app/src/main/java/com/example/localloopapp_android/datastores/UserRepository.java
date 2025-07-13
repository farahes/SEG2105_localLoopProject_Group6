package com.example.localloopapp_android.datastores;

import com.example.localloopapp_android.models.accounts.UserAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.localloopapp_android.models.accounts.OrganizerAccount;
import com.example.localloopapp_android.models.accounts.ParticipantAccount;

public class UserRepository {
    private final DatabaseReference dbRef;

    public UserRepository() {
        dbRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public void getUser(String userId, final UserCallback callback) {
        dbRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    callback.onSuccess(null);
                    return;
                }

                String role = dataSnapshot.child("role").getValue(String.class);
                UserAccount user = null;

                if ("Participant".equals(role)) {
                    user = dataSnapshot.getValue(ParticipantAccount.class);
                } else if ("Organizer".equals(role)) {
                    user = dataSnapshot.getValue(OrganizerAccount.class);
                }

                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onFailure(new Exception("Could not deserialize user with role: " + role));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    public interface UserCallback {
        void onSuccess(UserAccount user);
        void onFailure(Exception e);
    }
}
