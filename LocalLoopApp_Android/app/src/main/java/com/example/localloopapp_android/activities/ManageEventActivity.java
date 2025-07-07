package com.example.localloopapp_android.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.models.Category;
import com.example.localloopapp_android.models.Event;
import com.example.localloopapp_android.utils.Constants;
import com.example.localloopapp_android.viewmodels.CategoryViewModel;
import com.example.localloopapp_android.viewmodels.OrganizerViewModel;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.libraries.places.api.Places;

import java.text.SimpleDateFormat;
import java.util.*;

public class ManageEventActivity extends AppCompatActivity {

    private EditText etName, etDesc, etFee, etLocation, etStartDate, etEndDate;
    private Spinner spinnerCategory;
    private Button btnCreateEvent;
    private ImageButton btnDelete, btnClose;
    private final Calendar eventStartCalendar = Calendar.getInstance();
    private final Calendar eventEndCalendar = Calendar.getInstance();
    private List<Category> categoryList = new ArrayList<>();
    private String selectedCategoryId = null;

    private OrganizerViewModel organizerViewModel;
    private CategoryViewModel categoryViewModel;

    private boolean isEditMode = false;
    private String organizerId;
    private Event eventToEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Initialize Places SDK if not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyD-cPvHb7hc9AosWqxRLWlRKu-wtMSxwFo");
        }
        
        // Get extras
        organizerId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);
        eventToEdit = getIntent().getSerializableExtra(Constants.EXTRA_EVENT_OBJECT, Event.class);
        isEditMode = eventToEdit != null;

        // Bind views
        etName = findViewById(R.id.etEventName);
        etDesc = findViewById(R.id.etEventDesc);
        etLocation = findViewById(R.id.etEventLocation);
        etLocation.setFocusable(false);
        etLocation.setOnClickListener(v -> {
            List<com.google.android.libraries.places.api.model.Place.Field> fields = Arrays.asList(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.NAME,
                com.google.android.libraries.places.api.model.Place.Field.ADDRESS,
                com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
            );
            Intent intent = new com.google.android.libraries.places.widget.Autocomplete.IntentBuilder(
                    com.google.android.libraries.places.widget.model.AutocompleteActivityMode.OVERLAY, fields)
                    .build(this);
            startActivityForResult(intent, 1001);
        });
        etFee = findViewById(R.id.etEventFee);
        etStartDate = findViewById(R.id.etEventStartDate);
        etEndDate = findViewById(R.id.etEventEndDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnDelete = findViewById(R.id.btnDelete);
        btnClose = findViewById(R.id.btnClose);

        // ViewModels
        organizerViewModel = new ViewModelProvider(this).get(OrganizerViewModel.class);
        organizerViewModel.setOrganizerId(organizerId);

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        // Setup logic
        setupCategorySpinner();
        setupDatePickers();

        if (isEditMode) {
            populateFieldsForEdit();
            btnCreateEvent.setText("Update Event");
        }

        // Delete button
        if (isEditMode) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Event")
                        .setMessage("Are you sure you want to delete this event?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            organizerViewModel.deleteEvent(eventToEdit);
                            Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        // Close & Create
        btnClose.setOnClickListener(v -> finish());

        btnCreateEvent.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String feeStr = etFee.getText().toString().trim();

            if (name.isEmpty() || desc.isEmpty() || location.isEmpty() || selectedCategoryId == null) {
                Toast.makeText(this, "Please fill all required fields and select a category", Toast.LENGTH_SHORT).show();
                return;
            }

            double fee = 0.0;
            if (!feeStr.isEmpty()) {
                try {
                    fee = Double.parseDouble(feeStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid fee amount", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            long start = eventStartCalendar.getTimeInMillis();
            long end = eventEndCalendar.getTimeInMillis();

            if (isEditMode) {
                organizerViewModel.updateEvent(eventToEdit, name, desc, selectedCategoryId, location, fee, start, end);
                Toast.makeText(this, "Event updated", Toast.LENGTH_SHORT).show();
            } else {
                organizerViewModel.createEvent(name, desc, selectedCategoryId, location, fee, start, end);
                Toast.makeText(this, "Event created", Toast.LENGTH_SHORT).show();
            }

            finish();
        });
    }

    private void populateFieldsForEdit() {
        etName.setText(eventToEdit.getName());
        etDesc.setText(eventToEdit.getDescription());
        etLocation.setText(eventToEdit.getLocation());
        etFee.setText(String.valueOf(eventToEdit.getFee()));
        eventStartCalendar.setTimeInMillis(eventToEdit.getEventStart());
        eventEndCalendar.setTimeInMillis(eventToEdit.getEventEnd());
        updateDateField(etStartDate, eventStartCalendar);
        updateDateField(etEndDate, eventEndCalendar);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        categoryViewModel.getCategories().observe(this, categories -> {
            categoryList = categories;

            List<String> names = new ArrayList<>();
            for (Category c : categories) {
                names.add(c.getName());
            }

            adapter.clear();
            adapter.addAll(names);
            adapter.notifyDataSetChanged();

            // Pre-select if editing
            if (isEditMode) {
                for (int i = 0; i < categoryList.size(); i++) {
                    if (categoryList.get(i).getCategoryId().equals(eventToEdit.getCategoryId())) {
                        spinnerCategory.setSelection(i);
                        selectedCategoryId = categoryList.get(i).getCategoryId();
                        break;
                    }
                }
            }
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId = categoryList.get(position).getCategoryId();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = null;
            }
        });
    }

    private void setupDatePickers() {
        // Start Date Picker
        etStartDate.setOnClickListener(v -> {
            int y = eventStartCalendar.get(Calendar.YEAR);
            int m = eventStartCalendar.get(Calendar.MONTH);
            int d = eventStartCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog startDialog = new DatePickerDialog(this, (view, year, month, day) -> {
                eventStartCalendar.set(year, month, day);
                updateDateField(etStartDate, eventStartCalendar);

                // Set end date to match start date
                eventEndCalendar.setTimeInMillis(eventStartCalendar.getTimeInMillis());
                updateDateField(etEndDate, eventEndCalendar);
            }, y, m, d);

            startDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            startDialog.show();
        });

        // End Date Picker
        etEndDate.setOnClickListener(v -> {
            int y = eventEndCalendar.get(Calendar.YEAR);
            int m = eventEndCalendar.get(Calendar.MONTH);
            int d = eventEndCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog endDialog = new DatePickerDialog(this, (view, year, month, day) -> {
                eventEndCalendar.set(year, month, day);
                updateDateField(etEndDate, eventEndCalendar);
            }, y, m, d);

            endDialog.getDatePicker().setMinDate(eventStartCalendar.getTimeInMillis());
            endDialog.show();
        });
    }

    private void updateDateField(EditText editText, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                etLocation.setText(place.getAddress());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }
            // else if (resultCode == RESULT_CANCELED) { user pressed back—no op }
        }
    }
}

