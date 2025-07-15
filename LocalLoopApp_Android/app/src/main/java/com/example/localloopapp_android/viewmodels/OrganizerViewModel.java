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
     */
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
     * Creates a new event.
     */
    public Event createEvent(String name, String description,
                             String categoryId, String location, double fee, long eventStart, long eventEnd) {
        Event event = eventRepo.addEvent(organizerId, name, description, categoryId, location, fee, eventStart, eventEnd);
        fetchEventsByOrganizer();
        return event;
    }

    /**
     * Deletes an event.
     */
    public void deleteEvent(Event eventToDelete) {
        eventRepo.deleteEvent(eventToDelete);
        fetchEventsByOrganizer();
    }

    /**
     * Updates an existing event.
     */
    public void updateEvent(Event eventToEdit, String newName, String newDescription,
                            String newCategoryId, String newLocation, double newFee, long newEventStart, long newEventEnd) {
        // Update event fields
        eventToEdit.setName(newName);
        eventToEdit.setDescription(newDescription);
        eventToEdit.setCategoryId(newCategoryId);
        eventToEdit.setLocation(newLocation);
        eventToEdit.setFee(newFee);
        eventToEdit.setEventStart(newEventStart);
        eventToEdit.setEventEnd(newEventEnd);

        eventRepo.editEvent(
                eventToEdit,
                newName,
                newDescription,
                newCategoryId,
                newLocation,
                newFee,
                newEventStart,
                newEventEnd
        ); // send updated object to repo
        fetchEventsByOrganizer();
    }
}
