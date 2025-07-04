package com.example.localloopapp_android.models;

import java.io.Serializable;

/**
 * A simple model class | model data transfer object (DTO)
 * DOES NOT contain any business logic or behavior.
 */
public class Event implements Serializable {

    private String eventId;
    private String organizerId;
    private String name;
    private String description;
    private String categoryId;
    private String location; // ✅ New field added
    private double fee;
    private long eventStart, eventEnd;
    private boolean eventActive;
    private final long creationDate = System.currentTimeMillis(); // can't modify event creation date

    public Event() {
        // Needed for Firebase deserialization
    }

    /**
     * Instantiates an event object with eventId, organizerId, name of event,
     * description, categoryId, location, monetary fee, event start and end dates & status.
     */
    public Event(String eventId, String organizerId, String name, String description,
                 String categoryId, String location, double fee, long eventStart, long eventEnd) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.location = location;
        this.fee = fee;
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
        this.eventActive = true;
    }

    // Getters
    public String getEventId() { return eventId; }
    public String getOrganizerId() { return organizerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategoryId() { return categoryId; }
    public String getLocation() { return location; }
    public double getFee() { return fee; }
    public long getCreationDate() { return creationDate; }
    public long getEventStart() { return eventStart; }
    public long getEventEnd() { return eventEnd; }
    public boolean isEventActive() { return eventActive; }

    // Setters
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public void setLocation(String location) { this.location = location; }
    public void setFee(double fee) { this.fee = fee; }
    public void setEventStart(long eventStart) { this.eventStart = eventStart; }
    public void setEventEnd(long eventEnd) { this.eventEnd = eventEnd; }

    // Domain mutators
    public void disableEvent() {
        if (eventActive) {
            this.eventActive = false;
        }
    }

    public void enableEvent() {
        if (!eventActive) {
            this.eventActive = true;
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
                ", location='" + location + '\'' +
                ", fee=" + fee +
                ", creationDate=" + creationDate +
                ", eventStart=" + eventStart +
                ", eventEnd=" + eventEnd +
                ", eventActive=" + eventActive +
                '}');
    }
}
