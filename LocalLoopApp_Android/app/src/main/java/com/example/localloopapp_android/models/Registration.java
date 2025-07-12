package com.example.localloopapp_android.models;

public class Registration {
    private String registrationId;
    private String eventId;
    private String participantId;
    private String organizerId;
    private String status; // e.g., "pending", "approved", "rejected"
    private long timestamp;

    public Registration() {
        // Default constructor required for calls to DataSnapshot.getValue(Registration.class)
    }

    public Registration(String registrationId, String eventId, String participantId, String status, long timestamp) {
        this.registrationId = registrationId;
        this.eventId = eventId;
        this.participantId = participantId;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
