package com.example.localloopapp_android.datastores;

import android.util.Log;

import com.example.localloopapp_android.models.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * crud
 * EventRepository manages creation, update, deletion and retrieval of Event objects.
 * Syncs with Firebase and keeps in-memory cache for performance.
 */
public class EventRepository {

    private final List<Event> myEvents = new ArrayList<>();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("events");

    public interface EventCallback {
        void onSuccess(List<Event> events);
        void onFailure(Exception e);
    }

    /**
     * Creates a new Event and adds it to Firebase and memory.
     */
    public Event addEvent(String organizerId, String name, String description, String categoryId,
                          String location, double fee, long eventStart, long eventEnd) {

        String eventId = dbRef.push().getKey();
        Event e = new Event(eventId, organizerId, name, description, categoryId, location, fee, eventStart, eventEnd);

        dbRef.child(eventId).setValue(e);
        myEvents.add(e);
        return e;
    }


    /**
     * Updates the properties of an existing Event in Firebase and memory.
     */
    public void editEvent(Event eventToEdit, String newName, String newDescription,
                          String newCategoryId, String newLocation, double newFee,
                          long newEventStart, long newEventEnd) {

        eventToEdit.setName(newName);
        eventToEdit.setDescription(newDescription);
        eventToEdit.setCategoryId(newCategoryId);
        eventToEdit.setLocation(newLocation);
        eventToEdit.setFee(newFee);
        eventToEdit.setEventStart(newEventStart);
        eventToEdit.setEventEnd(newEventEnd);

        dbRef.child(eventToEdit.getEventId()).setValue(eventToEdit);
    }


    /**
     * Removes an event from Firebase and in-memory cache.
     */
    public void deleteEvent(Event e) {
        e.disableEvent(); // Optional: mark locally as inactive. kinda redundant tho since the event is immediately deleted.
        dbRef.child(e.getEventId()).removeValue();
        myEvents.remove(e);
    }

    /**
     * Returns unmodifiable list of cached events.
     */
    public List<Event> getMyEvents() {
        return Collections.unmodifiableList(myEvents);
    }

    /**
     * Fetch all events created by this organizer from Firebase.
     * Optionally call this in ViewModel to trigger sync.
     */
    public void fetchEventsByOrganizer(String organizerId, EventCallback callback) {
        dbRef.orderByChild("organizerId").equalTo(organizerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    /**
                     * Called by Firebase when the data at the requested location is read successfully.
                     * It clears the local cache (myEvents.clear()),
                     * iterates through the returned children, converts each to an Event, and adds it to the cache
                     */
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        myEvents.clear(); // clears the local cache before fetching new data
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Event e = child.getValue(Event.class);
                            myEvents.add(e);
                        }
                        callback.onSuccess(myEvents); // return events to callback
                    }

                    /**
                     * called if the read operation is canceled or fails (e.g., due to permission issues or network errors).
                     * Simply passes the exception to the callback.
                     * @param error A description of the error that occurred
                     */
                    @Override
                    public void onCancelled(DatabaseError error) {
                        //Log.e("EventRepository", "Failed to load events", error.toException());
                        callback.onFailure(error.toException()); // return error to callback
                    }
                });
    }
}
