package com.example.localloopapp_android.viewmodels;

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

    private final EventRepository eventRepo;

    public OrganizerViewModel() {
        this.eventRepo = new EventRepository();  // could be injected in the future
    }

    /**
     * Creates a new event and stores it in memory (or Firebase in the future).
     *
     * @param organizerId   ID of the organizer creating the event
     * @param name          Name of the event
     * @param description   Description of the event
     * @param categoryId    Category ID from the admin-defined list
     * @param fee           Participation fee for the event
     * @param eventStart    Start date/time of the event (epoch ms)
     * @param eventEnd      End date/time of the event (epoch ms)
     * @return              The created Event object
     */
    public Event createEvent(String organizerId, String name, String description,
                             String categoryId, double fee, long eventStart, long eventEnd) {
        return eventRepo.addEvent(organizerId, name, description, categoryId, fee, eventStart, eventEnd);
    }

    /**
     * Deletes an existing event from the data store.
     * Currently marks it as inactive and removes from the in-memory list.
     *
     * @param eventToDelete The Event object to be deleted
     */
    public void deleteEvent(Event eventToDelete) {
        eventRepo.deleteEvent(eventToDelete);
    }

    /**
     * Updates the details of an existing event.
     *
     * @param eventToEdit   The existing Event object to modify
     * @param newName       New event name
     * @param newDescription New event description
     * @param newCategoryId New category ID
     * @param newFee        Updated participation fee
     * @param newStart      Updated start time (epoch ms)
     * @param newEnd        Updated end time (epoch ms)
     */
    public void updateEvent(Event eventToEdit, String newName, String newDescription,
                            String newCategoryId, double newFee, long newStart, long newEnd) {
        eventRepo.editEvent(eventToEdit, newName, newDescription, newCategoryId, newFee, newStart, newEnd);
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
