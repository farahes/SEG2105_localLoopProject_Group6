package com.example.localloopapp_android.datastores;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.localloopapp_android.models.Registration;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class RegistrationRepository {

    private DatabaseReference databaseReference;

    public RegistrationRepository() {
        databaseReference = FirebaseDatabase.getInstance().getReference("registrations");
    }

    public LiveData<List<Registration>> getPendingRegistrationsForOrganizer(String organizerId) {
        MutableLiveData<List<Registration>> liveData = new MutableLiveData<>();
        databaseReference.orderByChild("organizerId").equalTo(organizerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Registration> registrations = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Registration registration = snapshot.getValue(Registration.class);
                            if (registration != null && "pending".equals(registration.getStatus())) {
                                registrations.add(registration);
                            }
                        }
                        liveData.setValue(registrations);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle error
                    }
                });
        return liveData;
    }

    public void updateRegistrationStatus(String registrationId, String newStatus) {
        databaseReference.child(registrationId).child("status").setValue(newStatus);
    }
}
