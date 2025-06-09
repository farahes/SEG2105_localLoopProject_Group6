package com.example.localloopapp_android.services;

import java.util.List;

/**
 * Handles participant-specific operations like searching, joining, and managing event participation.
 * Invoked by ParticipantDashboardActivity or EventSearchActivity.
 */
public class ParticipantService {

    /**
     * Registers the participant for an event by adding a join request to the event.
     * @param eventId ID of the event
     * @param participantId UID of the participant
     */
    public void registerForEvent(String eventId, String participantId) {
        // TODO: Add participant to /eventJoinRequests/{eventId}/{participantId} = "pending"
    }

    /**
     * Cancels a join request for an event (before it’s approved).
     * @param eventId ID of the event
     * @param participantId UID of the participant
     */
    public void unregisterFromEvent(String eventId, String participantId) {
        // TODO: Remove /eventJoinRequests/{eventId}/{participantId}
    }

    /**
     * Searches for events by keyword in name or by category.
     * @param query Text input or category ID
     * @return List of events matching the search
     */
    public List<Object> searchEvents(String query) {
        // TODO: Query Firebase for matching events
        return null;
    }

    /**
     * Returns the status of this participant's join request for a specific event.
     * @param eventId Event ID
     * @param participantId UID of participant
     * @return Request status ("pending", "approved", "rejected")
     */
    public String getJoinRequestStatus(String eventId, String participantId) {
        // TODO: Fetch /eventJoinRequests/{eventId}/{participantId}
        return null;
    }
}
