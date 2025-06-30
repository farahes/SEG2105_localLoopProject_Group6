package com.example.localloopapp_android.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.localloopapp_android.datastores.EventRepository;
import com.example.localloopapp_android.models.Event;

import java.util.List;

/**
 * OrganizerViewModel
 *
 * Handles organizer-specific operations like creating or managing events.
 * Called by OrganizerDashboardActivity or other UI controllers.
 * Does not reference UI directly and survives configuration changes.
 */
public class OrganizerViewModel extends ViewModel {
    private String organizerId; // ID of the organizer, set when ViewModel is created
    private final EventRepository eventRepo;
    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();


    public OrganizerViewModel() {
        this.eventRepo = new EventRepository();  // could be injected in the future
        // no refresh on create since we'd need to pass organizerId in the constructor -> need a factory method or similar
    }

    public LiveData<List<Event>> getEventsLiveData() {
        return eventsLiveData;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
        fetchEventsByOrganizer(); // refresh events when ID changes
    }

    /**
     * Fetches all events created by this organizer from firebase.
     * Updates the LiveData with the list of events.
     * This will be called when the ViewModel is initialized or when the user navigates to the organizer dashboard.
     */
    // facade method to fetch events by organizer ID
    public void fetchEventsByOrganizer() {
        if (organizerId == null) {
            Log.e("OrganizerViewModel", "Organizer ID is not set. Cannot fetch events.");
            return;
        }
        eventRepo.fetchEventsByOrganizer(this.organizerId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventsLiveData.setValue(events);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("OrganizerViewModel", "Failed to load events", e);
            }
        });
    }

    /**
     * Creates a new event and stores it in memory (or Firebase in the future).
     *
     * @param name          Name of the event
     * @param description   Description of the event
     * @param categoryId    Category ID from the admin-defined list
     * @param fee           Participation fee for the event
     * @param eventStart    Start date/time of the event (epoch ms)
     * @param eventEnd      End date/time of the event (epoch ms)
     * @return              The created Event object
     */
    public Event createEvent(String name, String description,
                             String categoryId, double fee, long eventStart, long eventEnd) {
        Event event = eventRepo.addEvent(organizerId, name, description, categoryId, fee, eventStart, eventEnd);
        fetchEventsByOrganizer(); // Refresh the list after creation
        return event;
    }

    /**
     * Deletes an existing event from the data store.
     * Currently marks it as inactive and removes from the in-memory list.
     *
     * @param eventToDelete The Event object to be deleted
     */
    public void deleteEvent(Event eventToDelete) {
        eventRepo.deleteEvent(eventToDelete);
        fetchEventsByOrganizer(); // Refresh the list after deletion
    }

    /**
     * Updates the details of an existing event.
     *
     * @param eventToEdit   The existing Event object to modify
     * @param newName       New event name
     * @param newDescription New event description
     * @param newCategoryId New category ID
     * @param newFee        Updated participation fee
     * @param newEventStart      Updated start time (epoch ms)
     * @param newEventEnd        Updated end time (epoch ms)
     */
    public void updateEvent(Event eventToEdit, String newName, String newDescription,
                            String newCategoryId, double newFee, long newEventStart, long newEventEnd) {
        eventRepo.editEvent(eventToEdit, newName, newDescription, newCategoryId, newFee, newEventStart, newEventEnd);
        fetchEventsByOrganizer(); // Refresh the list after update
    }

    /**
     * Returns a list of all events created by the current organizer.
     * In the future, this can be filtered or fetched from Firebase.
     *
     * @return Unmodifiable list of Event objects
     */
    public List<Event> getMyEvents() {
        return eventRepo.getMyEvents();
    }
}
