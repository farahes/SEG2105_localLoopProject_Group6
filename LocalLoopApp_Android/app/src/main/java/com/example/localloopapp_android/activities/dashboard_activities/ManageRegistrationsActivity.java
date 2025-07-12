package com.example.localloopapp_android.activities.dashboard_activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.utils.Constants;
import com.google.firebase.database.*;

public class ManageRegistrationsActivity extends AppCompatActivity {

    private LinearLayout eventContainer;
    private String organizerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_registrations_organizer);

        eventContainer = findViewById(R.id.eventContainer);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        organizerId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);

        if (organizerId == null || organizerId.isEmpty()) {
            Toast.makeText(this, "Error: Organizer ID not found", Toast.LENGTH_LONG).show();
            return;
        }

        loadEventsAndRegistrations();
    }

    private void loadEventsAndRegistrations() {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");

        eventsRef.orderByChild("organizerId").equalTo(organizerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        eventContainer.removeAllViews();
                        for (DataSnapshot eventSnap : snapshot.getChildren()) {
                            String eventId = eventSnap.getKey();
                            String eventName = eventSnap.child("name").getValue(String.class);
                            long max = eventSnap.child("maxParticipants").getValue(Long.class) != null ?
                                    eventSnap.child("maxParticipants").getValue(Long.class) : 0;

                            loadEventCard(eventId, eventName, max);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ManageRegistrationsActivity.this,
                                "Failed to load events: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadEventCard(String eventId, String eventName, long max) {
        View eventCard = getLayoutInflater().inflate(R.layout.card_event_with_registrations, eventContainer, false);
        TextView tvEventName = eventCard.findViewById(R.id.tvEventName);
        TextView tvCapacityScore = eventCard.findViewById(R.id.tvEventCapacity);
        LinearLayout participantContainer = eventCard.findViewById(R.id.participantContainer);
        LinearLayout acceptedAttendeesContainer = eventCard.findViewById(R.id.acceptedAttendeesContainer);

        tvEventName.setText(eventName);

        DatabaseReference regRef = FirebaseDatabase.getInstance().getReference("registrations");

        regRef.orderByChild("eventId").equalTo(eventId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        participantContainer.removeAllViews();
                        int acceptedCount = 0;

                        for (DataSnapshot regSnap : snapshot.getChildren()) {
                            String status = regSnap.child("status").getValue(String.class);
                            String participantId = regSnap.child("participantId").getValue(String.class);
                            String registrationId = regSnap.getKey();

                            if ("accepted".equals(status)) {
                                acceptedCount++;
                                continue;
                            }

                            View participantRow = getLayoutInflater().inflate(R.layout.item_registration_row, participantContainer, false);
                            TextView tvName = participantRow.findViewById(R.id.tvParticipantName);
                            TextView tvEmail = participantRow.findViewById(R.id.tvParticipantEmail);
                            TextView tvStatus = participantRow.findViewById(R.id.tvParticipantStatus);
                            View btnAccept = participantRow.findViewById(R.id.btnAccept);
                            View btnReject = participantRow.findViewById(R.id.btnReject);

                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(participantId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot userSnap) {
                                            String name = userSnap.child("name").getValue(String.class);
                                            String username = userSnap.child("username").getValue(String.class);
                                            String email = userSnap.child("email").getValue(String.class);

                                            tvName.setText("Name: " + (name != null ? name : "(not set)") + (username != null ? " (" + username + ")" : ""));
                                            tvEmail.setText("Email: " + (email != null ? email : "(not set)"));
                                            tvStatus.setText("Status: " + status);

                                            btnAccept.setOnClickListener(v -> updateStatus(registrationId, eventId, participantId, "accepted", acceptedAttendeesContainer));
                                            btnReject.setOnClickListener(v -> updateStatus(registrationId, eventId, participantId, "rejected", acceptedAttendeesContainer));
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {}
                                    });

                            participantContainer.addView(participantRow);
                        }

                        tvCapacityScore.setText(acceptedCount + " / " + max);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ManageRegistrationsActivity.this,
                                "Failed to load registrations: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Load accepted attendees' names
        loadAcceptedAttendees(eventId, acceptedAttendeesContainer);

        eventContainer.addView(eventCard);
    }

    private void loadAcceptedAttendees(String eventId, LinearLayout container) {
        DatabaseReference acceptedRef = FirebaseDatabase.getInstance().getReference("acceptedAttendees").child(eventId);
        acceptedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                container.removeAllViews();
                for (DataSnapshot attendeeSnap : snapshot.getChildren()) {
                    String participantId = attendeeSnap.getKey();

                    FirebaseDatabase.getInstance().getReference("users").child(participantId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot userSnap) {
                                    String name = userSnap.child("name").getValue(String.class);
                                    String username = userSnap.child("username").getValue(String.class);

                                    TextView tv = new TextView(ManageRegistrationsActivity.this);
                                    tv.setText((name != null ? name : "(not set)") + (username != null ? " (" + username + ")" : ""));
                                    tv.setTextColor(getResources().getColor(R.color.green, null));
                                    container.addView(tv);
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {}
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void updateStatus(String registrationId, String eventId, String participantId, String status, LinearLayout acceptedContainer) {
        DatabaseReference regRef = FirebaseDatabase.getInstance()
                .getReference("registrations")
                .child(registrationId)
                .child("status");

        regRef.setValue(status).addOnSuccessListener(v -> {
            Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();

            if ("accepted".equals(status)) {
                FirebaseDatabase.getInstance().getReference("acceptedAttendees")
                        .child(eventId).child(participantId).setValue(true);

                // Refresh accepted list
                loadAcceptedAttendees(eventId, acceptedContainer);
            } else {
                FirebaseDatabase.getInstance().getReference("acceptedAttendees")
                        .child(eventId).child(participantId).removeValue();
                loadAcceptedAttendees(eventId, acceptedContainer);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
