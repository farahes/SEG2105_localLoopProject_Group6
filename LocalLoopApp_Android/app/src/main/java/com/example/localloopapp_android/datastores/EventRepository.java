package com.example.localloopapp_android.datastores;

import com.example.localloopapp_android.models.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A placeholder repository for Event objects.
 * Currently manages an in-memory list of events for an organizer,
 * to be extended later for Firebase or other persistence.
 *
 * It stores & retrieves event objects, but doesn’t know who’s using them or why.
 * Handles basic CRUD operations:
 * - create, read, update, delete events.
 */
public class EventRepository {

    // memory store of events, replace or back with firebase in the future.
    private final List<Event> myEvents = new ArrayList<>();

    /**
     * Creates a new Event and adds it to the in-memory list.
     * The Event ID and persistence will be handled later.
     */
    public Event createEvent(String organizerId, String name, String description, String categoryId,
                             double fee, long eventStart, long eventEnd) {
        Event e = new Event(
                /* eventId */ null,
                /* organizerId */ organizerId,
                /* name */ name,
                /* description */ description,
                /* categoryId */ categoryId,
                /* fee */ fee,
                /* eventStart */ eventStart,
                /* eventEnd */ eventEnd
        );
        myEvents.add(e);
        return e;
    }

    /**
     * Updates the properties of an existing Event in the in-memory list.
     */
    public void editEvent(Event eventToEdit, String newName, String newDescription, String newCategoryId,
                          double newFee, long newEventStart, long newEventEnd) {
        eventToEdit.setName(newName);
        eventToEdit.setDescription(newDescription);
        eventToEdit.setCategoryId(newCategoryId);
        eventToEdit.setFee(newFee);
        eventToEdit.setEventStart(newEventStart);
        eventToEdit.setEventEnd(newEventEnd);
    }

    /**
     * Marks an Event as inactive and removes it from the in-memory list.
     */
    public void deleteEvent(Event e) {
        e.disableEvent();
        myEvents.remove(e);
    }

    /**
     * Returns an unmodifiable view of all stored events.
     */
    public List<Event> getMyEvents() {
        return Collections.unmodifiableList(myEvents);
    }

    // TODO: Integrate Firebase persistence here in future updates
}