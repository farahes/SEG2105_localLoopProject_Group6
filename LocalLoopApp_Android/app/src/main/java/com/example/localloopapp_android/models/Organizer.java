package com.example.localloopapp_android.models;

import com.example.localloopapp_android.datastores.Event;
import com.example.localloopapp_android.datastores.EventRepository;
import java.util.List;

/**
 * An Organizer is a User who creates and manages Event objects.
 * It delegates in-memory datastore operations to EventRepository.
 */
public class Organizer extends User {
    private String companyName;
    private final EventRepository eventRepo = new EventRepository();

    public Organizer() {
        super();
    }

    public Organizer(String userID, String firstName, String lastName, String username,
                     String email, String phoneNumber, String role, String companyName) {
        super(userID, firstName, lastName, username, email, phoneNumber, role);
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * Creates a new Event for this organizer via the repository.
     * Returns the created Event (ID may be assigned later).
     */
    public Event createEvent(String name, String description, String categoryId,
                             double fee, long eventStart, long eventEnd) {
        return eventRepo.createEvent(
                getUserID(), name, description, categoryId, fee, eventStart, eventEnd);
    }

    /**
     * Updates an existing Event via the repository.
     */
    public void editEvent(Event eventToEdit, String newName, String newDescription, String newCategoryId,
                          double newFee, long newEventStart, long newEventEnd) {
        eventRepo.editEvent(
                eventToEdit, newName, newDescription, newCategoryId, newFee, newEventStart, newEventEnd);
    }

    /**
     * Deletes an Event via the repository.
     */
    public void deleteEvent(Event e) {
        eventRepo.deleteEvent(e);
    }

    /**
     * Returns all Events created by this organizer.
     */
    public List<Event> getMyEvents() {
        return eventRepo.getMyEvents();
    }
}
