package com.example.localloopapp_android.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.localloopapp_android.models.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventViewModel extends ViewModel {
    private final MutableLiveData<List<Event>> events = new MutableLiveData<>(new ArrayList<>());
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("events");

    public LiveData<List<Event>> getEvents() {
        return events;
    }

    // Fetch all events from Firebase
    public void fetchEvents() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Event> eventList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Event e = child.getValue(Event.class);
                    if (e != null && e.isEventActive()) {
                        eventList.add(e);
                    }
                }
                events.postValue(eventList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Optionally handle error
            }
        });
    }
}