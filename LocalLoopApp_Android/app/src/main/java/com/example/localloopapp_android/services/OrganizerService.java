package com.example.localloopapp_android.services;

import com.example.localloopapp_android.datastores.EventRepository;
import com.example.localloopapp_android.models.Event;

import java.util.List;

/**
 * Handles organizer-specific operations like creating or managing events.
 * Called by OrganizerDashboardActivity. Does not know about UI.
 */
public class OrganizerService {

    private final EventRepository eventRepo;

    public OrganizerService() {
        this.eventRepo = new EventRepository();  // could be injected in future
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
        return eventRepo.createEvent(organizerId, name, description, categoryId, fee, eventStart, eventEnd);
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

/**
 * OrganizerService acts as a business logic layer between the UI (e.g. OrganizerDashboardActivity)
 * and the data layer (EventRepository). It coordinates event-related operations while keeping
 * Activities clean and free of logic or direct data access.
 *
 * Why use a service layer instead of calling EventRepository directly?
 *
 * - ✅ Input Validation: Service methods can enforce rules (e.g., fee must be ≥ 0, dates must make sense)
 * - ✅ Business Logic: You can add future behaviors like checking role permissions or status
 * - ✅ Extensibility: Easily add analytics, logging, or undo/redo behavior here
 * - ✅ Abstraction: Hides how events are stored or fetched — you can swap out or adapt the underlying repository later
 *
 * This keeps the architecture clean, testable, and scalable.
 */
