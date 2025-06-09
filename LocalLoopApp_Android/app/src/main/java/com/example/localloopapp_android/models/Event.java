package com.example.localloopapp_android.models;

/**
 * A simple model class | model data transfer object (DTO)
 * DOES NOT contain any business logic or behavior.
 */
public class Event {

    private String eventId;
    private String organizerId;
    private String name;
    private String description;
    private String categoryId;
    private double fee;
    private long eventStart, eventEnd;
    private boolean eventActive;
    private final long creationDate = System.currentTimeMillis(); // can't modify event creation date

    public Event() {
        // somehow needed for firebase deserialization
    }

    /**
     * Instantiates an event object with eventId, organizerId, name of event,
     * description of event, categoryId, monetary fee, event start and end dates & status.
     */
    public Event(String eventId, String organizerId, String name, String description,
                 String categoryId, double fee, long eventStart, long eventEnd) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.fee = fee;
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
        this.eventActive = true;
    }

    // getters
    public String getEventId() { return eventId; }
    public String getOrganizerId() { return organizerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategoryId() { return categoryId; }
    public double getFee() { return fee; }
    public long getCreationDate() { return creationDate; }
    public long getEventStart() { return eventStart; }
    public long getEventEnd() { return eventEnd; }
    public boolean isEventActive() { return eventActive; }

    // setters
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public void setFee(double fee) { this.fee = fee; }
    public void setEventStart(long eventStart) { this.eventStart = eventStart; }
    public void setEventEnd(long eventEnd) { this.eventEnd = eventEnd; }

    // pure domain mutators
    public void disableEvent() {
        if (eventActive) {
            this.eventActive = false; // uncomment below when implemented into an activity
        }
    }
    public void enableEvent() {
        if (!eventActive) {
            this.eventActive = true; // for the future throw error if past end date
        }
    }

    @Override
    public String toString() {
        return ("Event{" +
                "eventId='" + eventId + '\'' +
                ", organizerId='" + organizerId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", fee='" + fee + '\'' +
                ", creationDate'" + creationDate + '\'' +
                ", eventStart='" + eventStart + '\'' +
                ", eventEnd='" + eventEnd + '\'' +
                ", eventActive='" + eventActive +
                '}');
    }
}