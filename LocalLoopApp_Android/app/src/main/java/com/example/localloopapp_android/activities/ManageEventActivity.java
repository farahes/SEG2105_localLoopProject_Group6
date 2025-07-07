package com.example.localloopapp_android.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.net.Uri;
import android.util.Base64;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

    private Button btnSelectImage;
    private ImageView ivEventImage;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // 1) Bind the new views
        btnSelectImage = findViewById(R.id.btnSelectImage);
        ivEventImage   = findViewById(R.id.ivEventImage);

// 2) Register the activity-result launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        // Show the image in the ImageView
                        ivEventImage.setImageURI(selectedImageUri);
                        ivEventImage.setVisibility(View.VISIBLE);
                        // Kick off the Base64 to Firebase upload
                        uploadEventImage(selectedImageUri);
                    }
                }
        );

// 3) Launch it when the button is clicked
        btnSelectImage.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
            pick.setType("image/*");
            pickImageLauncher.launch(pick);
        });

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
            loadEventImage(eventToEdit.getEventId(), ivEventImage);
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

    /**
     * Uploads the selected image URI into /avatars/{eventId}.jpg
     */
    private void uploadEventImage(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            Bitmap bmp = BitmapFactory.decodeStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
            String b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("avatars")
                    .child(eventToEdit.getEventId());

            ref.setValue(b64)
                    .addOnSuccessListener(v ->
                            Toast.makeText(this, "Image Successfully Uploaded!", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        } catch (Exception e) {
            Toast.makeText(this, "Failed to read image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Loads the Base64‐encoded image for the given eventId from
     * /avatars/{eventId} in Realtime Database, decodes it into a Bitmap,
     * and sets it on iv. On failure, shows a toast.
     */
    public void loadEventImage(String eventId, ImageView iv) {
        FirebaseDatabase.getInstance()
                .getReference("avatars")
                .child(eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snap) {
                        String b64 = snap.getValue(String.class);
                        if (b64 != null && !b64.isEmpty()) {
                            try {
                                int comma = b64.indexOf(',');
                                if (comma >= 0) b64 = b64.substring(comma + 1);
                                byte[] data = Base64.decode(b64, Base64.DEFAULT);
                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                if (bmp != null) {
                                    iv.setImageBitmap(bmp);
                                    iv.setVisibility(View.VISIBLE);
                                    return;
                                }
                            } catch (Exception ignored) { }
                        }
                        // no image or decode failed
                        iv.setVisibility(View.GONE);
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        iv.setVisibility(View.GONE);
                    }
                });
    }

}

