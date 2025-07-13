package com.example.localloopapp_android.activities.dashboard_activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.localloopapp_android.R;

import java.util.ArrayList;
import java.util.List;

public class TicketsListFragment extends Fragment {
    private static final String ARG_IS_UPCOMING = "is_upcoming";
    private boolean isUpcoming;

    public static TicketsListFragment newInstance(boolean isUpcoming) {
        TicketsListFragment fragment = new TicketsListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_UPCOMING, isUpcoming);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isUpcoming = getArguments().getBoolean(ARG_IS_UPCOMING);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tickets_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TicketsAdapter(getMockTickets(isUpcoming)));
        return view;
    }

    private List<TicketItem> getMockTickets(boolean upcoming) {
        List<TicketItem> list = new ArrayList<>();
        if (upcoming) {
            list.add(new TicketItem(
                    R.drawable.ic_event_placeholder, // image resource
                    "Community Clean-Up",
                    "Help clean up the park with your neighbors!",
                    "Community",
                    "$0",
                    "July 20, 2025",
                    "10:00 AM",
                    "Central Park"
            ));
            list.add(new TicketItem(
                    R.drawable.ic_event_placeholder,
                    "Tech Meetup",
                    "Monthly meetup for tech enthusiasts.",
                    "Technology",
                    "$5",
                    "August 5, 2025",
                    "6:00 PM",
                    "Innovation Hub"
            ));
        } else {
            list.add(new TicketItem(
                    R.drawable.ic_event_placeholder,
                    "Spring Festival",
                    "Celebrate spring with food and music!",
                    "Festival",
                    "$10",
                    "May 12, 2025",
                    "2:00 PM",
                    "Town Square"
            ));
            list.add(new TicketItem(
                    R.drawable.ic_event_placeholder,
                    "Workshop: Art Basics",
                    "Learn the basics of drawing and painting.",
                    "Workshop",
                    "$15",
                    "June 1, 2025",
                    "11:00 AM",
                    "Art Center"
            ));
        }
        return list;
    }

    public static class TicketItem {
        public int eventImageResId;
        public String eventName;
        public String eventDescription;
        public String eventCategory;
        public String eventFee;
        public String eventDate;
        public String eventTime;
        public String eventLocation;

        // For simplicity, location as string; for real use, add LatLng, etc.
        public TicketItem(int imageResId, String name, String description, String category, String fee, String date, String time, String location) {
            eventImageResId = imageResId;
            eventName = name;
            eventDescription = description;
        }
    }
}
