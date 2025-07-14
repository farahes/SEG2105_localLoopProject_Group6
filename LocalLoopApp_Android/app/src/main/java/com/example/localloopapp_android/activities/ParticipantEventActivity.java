// LocalLoopApp_Android/app/src/main/java/com/example/localloopapp_android/ParticipantEventActivity.java
package com.example.localloopapp_android.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.localloopapp_android.R;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Address;
import android.location.Geocoder;
import java.util.List;
import java.util.Locale;
import android.os.PersistableBundle;

public class ParticipantEventActivity extends AppCompatActivity {
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_participant_event_card);

        // Get event name from intent and set it
        String eventName = getIntent().getStringExtra("event_name");
        TextView eventNameView = findViewById(R.id.tvEventName);
        if (eventName != null) {
            eventNameView.setText(eventName);
        }

        // Set event details/description
        String eventDetails = getIntent().getStringExtra("event_details");
        TextView eventDetailsView = findViewById(R.id.tvEventDescription);
        if (eventDetails != null) {
            eventDetailsView.setText(eventDetails);
        }

        // Set event category
        String eventCategoryId = getIntent().getStringExtra("event_category");
        TextView eventCategoryView = findViewById(R.id.tvEventCategory);
        if (eventCategoryId != null) {
            fetchCategoryName(eventCategoryId, eventCategoryView);
        } else {
            eventCategoryView.setText("Unknown");
        }

        // Set event fee
        double cost = getIntent().getDoubleExtra("event_cost", 0.0);
        TextView eventFeeView = findViewById(R.id.tvEventFee);
        if (cost == 0.0) {
            eventFeeView.setText("Free");
            eventFeeView.setTextColor(getResources().getColor(R.color.green, null));
            eventFeeView.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            eventFeeView.setText("$" + cost);
            eventFeeView.setTextColor(getResources().getColor(android.R.color.black, null));
            eventFeeView.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        // Set event date
        String eventDate = getIntent().getStringExtra("event_date");
        TextView eventDateView = findViewById(R.id.tvEventDate);
        if (eventDate != null) {
            eventDateView.setText(eventDate);
        }

        // Set event time (show only start if start == end)
        String eventTime = getIntent().getStringExtra("event_time");
        TextView eventTimeView = findViewById(R.id.tvEventTime);
        if (eventTime != null) {
            // event_time format: "HH:mm - HH:mm"
            String[] times = eventTime.split(" - ");
            if (times.length == 2 && times[0].equals(times[1])) {
                eventTimeView.setText(times[0]);
            } else {
                eventTimeView.setText(eventTime);
            }
        }

        // Load avatar image from Firebase if eventId is provided
        String eventId = getIntent().getStringExtra("event_id");
        ImageView avatarView = findViewById(R.id.ivEventAvatar);
        if (eventId != null) {
            loadEventImage(eventId, avatarView);
        } else {
            avatarView.setImageResource(R.drawable.ic_event_placeholder);
        }

        // Set event location in MapView
        String eventLocation = getIntent().getStringExtra("event_location");
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume(); // Needed to display the map immediately

        mapView.getMapAsync(googleMap -> {
            LatLng latLng = getLocationFromAddress(eventLocation);
            if (latLng != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                googleMap.addMarker(new MarkerOptions().position(latLng).title(eventLocation));
            }
        });

        // Register button
        Button registerBtn = findViewById(R.id.btnRegisterEvent);
        if (cost == 0.0) {
            registerBtn.setText("Register (Free)");
        } else {
            registerBtn.setText("Register ($" + cost + ")");
        }
        registerBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Registration request sent (dummy)", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Loads the Base64‐encoded image for the given eventId from
     * /avatars/{eventId} in Realtime Database, decodes it into a Bitmap,
     * and sets it on iv. On failure, shows a toast.
     */
    public static void loadEventImage(String eventId, ImageView iv) {
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
                        iv.setImageResource(R.drawable.ic_event_placeholder);
                        iv.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        iv.setImageResource(R.drawable.ic_event_placeholder);
                        iv.setVisibility(View.VISIBLE);
                    }
                });
    }

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

    private void fetchCategoryName(String categoryId, TextView categoryView) {
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

    private LatLng getLocationFromAddress(String strAddress) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(strAddress, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // MapView lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }
}