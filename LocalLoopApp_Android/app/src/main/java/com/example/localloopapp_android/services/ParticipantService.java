package com.example.localloopapp_android.services;

/**
 * Handles participant-specific operations like registering for events.
 * Called by ParticipantDashboardActivity or EventListActivity.
 */
public class ParticipantService {

    /**
     * Registers the participant for an event.
     * @param eventId ID of the event
     * @param userId UID of the participant
     */
    public void registerForEvent(String eventId, String userId) {
        // TODO: Add participant to /eventRegistrations/{eventId}
    }

    /**
     * Unregisters the participant from an event.
     * @param eventId ID of the event
     * @param userId UID of the participant
     */
    public void unregisterFromEvent(String eventId, String userId) {
        // TODO: Remove participant from event
    }
}
