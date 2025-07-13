package com.example.localloopapp_android.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.activities.ManageEventActivity;
import com.example.localloopapp_android.models.Category;
import com.example.localloopapp_android.models.Event;
import com.example.localloopapp_android.models.Registration;
import com.example.localloopapp_android.viewmodels.CategoryViewModel;
import com.example.localloopapp_android.viewmodels.EventViewModel;
import com.example.localloopapp_android.viewmodels.RegistrationViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.*;

import android.location.Address;
import android.location.Geocoder;
import android.content.Context;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ParticipantEventSearchActivity extends AppCompatActivity {

    private EditText etName, etDescription;
    private TextView tvSelectedCategories, tvSelectedDate, tvStartTime, tvEndTime;
    private Button btnSelectCategories, btnSelectDate, btnStartTime, btnEndTime, btnSearch;
    private ImageView loadingGif; // for loading indicator
    private Spinner feeSpinner;
    private LinearLayout resultsContainer;
    ArrayAdapter<String> feeAdapter;

    private List<Category> allCategories = new ArrayList<>();
    private boolean[] selectedCategories;
    private boolean hasSearched = false;
    private List<String> selectedCategoryIds = new ArrayList<>();
    private Calendar selectedDate = null;
    private Integer startHour = null, startMinute = null, endHour = null, endMinute = null;

    private CategoryViewModel categoryViewModel;
    private EventViewModel eventViewModel;
    private RegistrationViewModel registrationViewModel;
    private Map<String, String> registrationStatusMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_event_search);

        // Initialize UI components
        setupUI();
        setupFeeSpinner();
        setupEventObserver();
        setupRegistrationObserver();

        // Load categories
        categoryViewModel.getCategories().observe(this, categories -> {
            allCategories = categories;
            selectedCategories = new boolean[categories.size()];
        });
        categoryViewModel.fetchCategories();

        // Set up listeners for buttons
        setupListeners();
    }

    // setup methods
    private void setupUI() {
        // Set up the UI elements here
        etName = findViewById(R.id.etEventName);
        etDescription = findViewById(R.id.etEventDescription);
        tvSelectedCategories = findViewById(R.id.tvSelectedCategories);
        btnSelectCategories = findViewById(R.id.btnSelectCategories);
        feeSpinner = findViewById(R.id.spinnerFee);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndTime = findViewById(R.id.btnEndTime);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        btnSearch = findViewById(R.id.btnSearchEvents);
        resultsContainer = findViewById(R.id.resultsContainer);
        // loading gif
        loadingGif = findViewById(R.id.loadingGif);
        Glide.with(this).asGif().load(R.drawable.ic_loading_packman).into(loadingGif);

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        registrationViewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);
    }

    private void setupFeeSpinner() {
        feeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Any", "Free", "< $50", "> $50"});
        feeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feeSpinner.setAdapter(feeAdapter);
    }

    private void setupListeners() {
        btnSelectCategories.setOnClickListener(v -> showCategoryDialog());
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnStartTime.setOnClickListener(v -> showTimePicker(true));
        btnEndTime.setOnClickListener(v -> showTimePicker(false));
        btnSearch.setOnClickListener(v -> searchEvents());
    }

    private void setupRegistrationObserver() {
        String participantId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        registrationViewModel.loadParticipantRegistrations(participantId);
        registrationViewModel.getParticipantRegistrations().observe(this, registrations -> {
            registrationStatusMap.clear();
            for (Registration r : registrations) {
                registrationStatusMap.put(r.getEventId(), r.getStatus());
            }
            // Re-filter or re-display events to update button states
            if (hasSearched) {
                eventViewModel.fetchEvents();
            }
        });
    }

    private void setupEventObserver(){
        String nameQuery = etName.getText().toString().trim().toLowerCase();
        String descQuery = etDescription.getText().toString().trim().toLowerCase();
        String feeOption = (String) feeSpinner.getSelectedItem();

        eventViewModel.getEvents().observe(this, events -> {
            if (!hasSearched) {
                resultsContainer.removeAllViews();
                loadingGif.setVisibility(View.GONE);
                resultsContainer.setVisibility(View.VISIBLE);
                return; // Don't show anything until searched
            }
            loadingGif.setVisibility(View.GONE);
            resultsContainer.setVisibility(View.VISIBLE);
            resultsContainer.removeAllViews();

            resultsContainer.removeAllViews();
            for (Event event : events) {
                // Name similarity
                if (!TextUtils.isEmpty(nameQuery) && (event.getName() == null ||
                        !event.getName().toLowerCase().contains(nameQuery))) continue;
                // Description similarity
                if (!TextUtils.isEmpty(descQuery) && (event.getDescription() == null ||
                        !event.getDescription().toLowerCase().contains(descQuery))) continue;
                // Category filter
                if (!selectedCategoryIds.isEmpty() && (event.getCategoryId() == null ||
                        !selectedCategoryIds.contains(event.getCategoryId()))) continue;
                // Fee filter
                double fee = event.getFee();
                if ("Free".equals(feeOption) && fee != 0) continue;
                if ("< $50".equals(feeOption) && !(fee > 0 && fee < 50)) continue;
                if ("> $50".equals(feeOption) && !(fee > 50)) continue;
                // Date and time filter
                if (selectedDate != null) {
                    Calendar eventCal = Calendar.getInstance();
                    eventCal.setTime(new Date(event.getEventStart()));
                    if (eventCal.get(Calendar.YEAR) != selectedDate.get(Calendar.YEAR) ||
                            eventCal.get(Calendar.MONTH) != selectedDate.get(Calendar.MONTH) ||
                            eventCal.get(Calendar.DAY_OF_MONTH) != selectedDate.get(Calendar.DAY_OF_MONTH))
                        continue;
                    // Time range
                    int eventHour = eventCal.get(Calendar.HOUR_OF_DAY);
                    int eventMinute = eventCal.get(Calendar.MINUTE);
                    int eventTime = eventHour * 60 + eventMinute;
                    if (startHour != null && startMinute != null) {
                        int startTime = startHour * 60 + startMinute;
                        if (eventTime < startTime) continue;
                    }
                    if (endHour != null && endMinute != null) {
                        int endTime = endHour * 60 + endMinute;
                        if (eventTime > endTime) continue;
                    }
                }
                // Add event card
                View card = getLayoutInflater().inflate(R.layout.item_participant_event_card, resultsContainer, false);

                // Load and display the event image
                ImageView ivEventImage = card.findViewById(R.id.ivEventAvatar);
                ManageEventActivity.loadEventImage(event.getEventId(), ivEventImage);
                ((TextView) card.findViewById(R.id.tvEventName)).setText(event.getName());
                ((TextView) card.findViewById(R.id.tvEventDescription)).setText(event.getDescription());
                setCategoryName((TextView) card.findViewById(R.id.tvEventCategory), event.getCategoryId());
                TextView tvFee = card.findViewById(R.id.tvEventFee);
                if (event.getFee() == 0.0) {
                    tvFee.setText("Free");
                    tvFee.setTextColor(getResources().getColor(R.color.green, null)); // Use ContextCompat if needed
                    tvFee.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    tvFee.setText("$" + event.getFee());
                    tvFee.setTextColor(getResources().getColor(android.R.color.black, null));
                    tvFee.setTypeface(null, android.graphics.Typeface.NORMAL);
                }

                // Set date and time separately
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String dateStr = dateFormat.format(new Date(event.getEventStart()));
                String timeStr = timeFormat.format(new Date(event.getEventStart())) + " - " + timeFormat.format(new Date(event.getEventEnd()));
                ((TextView) card.findViewById(R.id.tvEventDate)).setText(dateStr);
                ((TextView) card.findViewById(R.id.tvEventTime)).setText(timeStr);

                MapView mapView = card.findViewById(R.id.mapView);
                mapView.onCreate(null);  // pass Bundle if you have one
                mapView.getMapAsync(googleMap -> {
                    // Geocode your address or use LatLng directly
                    LatLng location = getLocationFromAddress(card.getContext(), event.getLocation());
                    if (location != null) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                        googleMap.addMarker(new MarkerOptions().position(location).title(event.getLocation()));
                    }
                });

                // Event registration
                Button btnRegister = card.findViewById(R.id.btnRegisterEvent);
                String status = registrationStatusMap.get(event.getEventId());

                if ("registered".equals(status)) {
                    btnRegister.setText("Registered");
                    btnRegister.setEnabled(false);
                } else {
                    // Default to Register
                    btnRegister.setText(event.getFee() == 0.0 ? "Register (Free)" : "Register ($" + event.getFee() + ")");
                    btnRegister.setEnabled(true);
                    btnRegister.setOnClickListener(v -> {
                        registerForEvent(event);
                    });
                }

                // Overlay button to open detailed event view
                Button btnCardOverlay = card.findViewById(R.id.btnCardOverlay);
                btnCardOverlay.setOnClickListener(v -> {
                    showEventCardPopup(event);
                });

                resultsContainer.addView(card);
            }
            if (resultsContainer.getChildCount() == 0) {
                TextView tv = new TextView(this);
                tv.setText("No events found.");
                resultsContainer.addView(tv);
            }
        });
    }

    // Dialogs and pickers
    private void showCategoryDialog() {
        String[] categoryNames = new String[allCategories.size()];
        for (int i = 0; i < allCategories.size(); i++) {
            categoryNames[i] = allCategories.get(i).getName();
        }
        new AlertDialog.Builder(this)
                .setTitle("Select Categories")
                .setMultiChoiceItems(categoryNames, selectedCategories, (dialog, which, isChecked) -> {
                    selectedCategories[which] = isChecked;
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    selectedCategoryIds.clear();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < selectedCategories.length; i++) {
                        if (selectedCategories[i]) {
                            selectedCategoryIds.add(allCategories.get(i).getCategoryId());
                            sb.append(allCategories.get(i).getName()).append(", ");
                        }
                    }
                    if (sb.length() > 0) sb.setLength(sb.length() - 2);
                    tvSelectedCategories.setText(sb.length() > 0 ? sb.toString() : "None");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDatePicker() {
        final Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker(boolean isStart) {
        final Calendar now = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            if (isStart) {
                startHour = hourOfDay;
                startMinute = minute;
                tvStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            } else {
                endHour = hourOfDay;
                endMinute = minute;
                tvEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
        dialog.show();
    }

    // Search events
    private void searchEvents() {
        hasSearched = true;
        //loading gif
        loadingGif.setVisibility(View.VISIBLE);
        resultsContainer.setVisibility(View.GONE);

        eventViewModel.fetchEvents();

    }

    // Helper methods
    private void registerForEvent(Event event) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String registrationId = databaseReference.child("registrations").push().getKey();
        String participantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Check if already registered
        if (registrationStatusMap.containsKey(event.getEventId())) {
            Toast.makeText(this, "You have already registered for this event.", Toast.LENGTH_SHORT).show();
            return;
        }

        Registration registration = new Registration(
                registrationId,
                event.getEventId(),
                participantId,
                "pending",
                System.currentTimeMillis()
        );
        registration.setOrganizerId(event.getOrganizerId());

        if (registrationId != null) {
            databaseReference.child("registrations").child(registrationId).setValue(registration)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ParticipantEventSearchActivity.this, "Registration request sent.", Toast.LENGTH_SHORT).show();
                        // Update local status
                        registrationStatusMap.put(event.getEventId(), "pending");
                        // Visually update the button
                        setupEventObserver(); // Re-run to update UI
                    })
                    .addOnFailureListener(e -> Toast.makeText(ParticipantEventSearchActivity.this, "Failed to send registration request.", Toast.LENGTH_SHORT).show());
        }
    }

    // Keep only one version of getLocationFromAddress
    public LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(strAddress, 1);
            if (addresses == null || addresses.isEmpty()) {
                return null; // No result found
            }
            Address location = addresses.get(0);
            return new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setCategoryName(TextView categoryView, String categoryId) {
        FirebaseDatabase.getInstance().getReference("categories").child(categoryId).child("name")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String name = snapshot.getValue(String.class);
                    if (name != null && !name.isEmpty()) {
                        categoryView.setText(name);
                    } else {
                        categoryView.setText("Unknown");
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    categoryView.setText("Unknown");
                }
            });
    }

    private void showEventCardPopup(Event event) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View popupView = getLayoutInflater().inflate(R.layout.item_participant_event_card, null);

        // Set up views in the popup
        ImageView ivEventImage = popupView.findViewById(R.id.ivEventAvatar);
        ManageEventActivity.loadEventImage(event.getEventId(), ivEventImage);

        ((TextView) popupView.findViewById(R.id.tvEventName)).setText(event.getName());
        ((TextView) popupView.findViewById(R.id.tvEventDescription)).setText(event.getDescription());
        setCategoryName((TextView) popupView.findViewById(R.id.tvEventCategory), event.getCategoryId());

        TextView tvFee = popupView.findViewById(R.id.tvEventFee);
        if (event.getFee() == 0.0) {
            tvFee.setText("Free");
            tvFee.setTextColor(getResources().getColor(R.color.green, null));
            tvFee.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            tvFee.setText("$" + event.getFee());
            tvFee.setTextColor(getResources().getColor(android.R.color.black, null));
            tvFee.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String dateStr = dateFormat.format(new Date(event.getEventStart()));
        String startTime = timeFormat.format(new Date(event.getEventStart()));
        String endTime = timeFormat.format(new Date(event.getEventEnd()));
        ((TextView) popupView.findViewById(R.id.tvEventDate)).setText(dateStr);
        ((TextView) popupView.findViewById(R.id.tvEventTime)).setText(
            startTime.equals(endTime) ? startTime : (startTime + " - " + endTime)
        );

        MapView mapView = popupView.findViewById(R.id.mapView);
        mapView.onCreate(null);
        mapView.getMapAsync(googleMap -> {
            LatLng location = getLocationFromAddress(popupView.getContext(), event.getLocation());
            if (location != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                googleMap.addMarker(new MarkerOptions().position(location).title(event.getLocation()));
            }
        });

        // Hosted By
        TextView hostedByView = popupView.findViewById(R.id.tvHostedBy);
        fetchOrganizerInfo(event.getOrganizerId(), hostedByView);

        // Register button with toast
        Button btnRegister = popupView.findViewById(R.id.btnRegisterEvent);
        btnRegister.setText(event.getFee() == 0.0 ? "Register (Free)" : "Register ($" + event.getFee() + ")");
        btnRegister.setOnClickListener(v -> {
            Toast.makeText(this, "TODO: hook up to registration logic", Toast.LENGTH_SHORT).show();
        });

        // Hide overlay button in popup
        Button btnCardOverlay = popupView.findViewById(R.id.btnCardOverlay);
        btnCardOverlay.setVisibility(View.GONE);

        dialog.setContentView(popupView);
        dialog.show();
    }

    // Copy fetchOrganizerInfo from ParticipantEventActivity for use here
    private void fetchOrganizerInfo(String organizerId, TextView hostedByView) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(organizerId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                String firstName = snap.child("firstName").getValue(String.class);
                String lastName = snap.child("lastName").getValue(String.class);
                String company = snap.child("companyName").getValue(String.class);
                String name = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
                if (name.isEmpty()) name = "Unknown";
                if (company == null || company.isEmpty()) company = "Unknown";
                hostedByView.setText("Hosted By: " + name + " on behalf of " + company);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                hostedByView.setText("Hosted By: Unknown");
            }
        });
    }

}
