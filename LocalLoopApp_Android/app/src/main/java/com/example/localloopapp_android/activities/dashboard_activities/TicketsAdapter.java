package com.example.localloopapp_android.activities.dashboard_activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.localloopapp_android.R;
import com.google.android.gms.maps.MapView;

import java.util.List;

public class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.TicketViewHolder> {
    private final List<TicketsListFragment.TicketItem> ticketList;

    public TicketsAdapter(List<TicketsListFragment.TicketItem> ticketList) {
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant_event_card, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TicketsListFragment.TicketItem item = ticketList.get(position);
holder.eventAvatar.setImageResource(item.eventImageResId);
holder.eventName.setText(item.eventName);
holder.eventDescription.setText(item.eventDescription);
holder.eventCategory.setText(item.eventCategory);
holder.eventFee.setText(item.eventFee);
holder.eventDate.setText(item.eventDate);
holder.eventTime.setText(item.eventTime);
// For mapView and eventLocation, you may want to set up a map marker or show the location string as appropriate.
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        ImageView eventAvatar;
        TextView eventName, eventDescription, eventCategory, eventFee, eventDate, eventTime;
        MapView mapView;
        TextView eventLocation;

        TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            eventAvatar = itemView.findViewById(R.id.ivEventAvatar);
            eventName = itemView.findViewById(R.id.tvEventName);
            eventDescription = itemView.findViewById(R.id.tvEventDescription);
            eventCategory = itemView.findViewById(R.id.tvEventCategory);
            eventFee = itemView.findViewById(R.id.tvEventFee);
            eventDate = itemView.findViewById(R.id.tvEventDate);
            eventTime = itemView.findViewById(R.id.tvEventTime);
            mapView = itemView.findViewById(R.id.mapView);
            eventLocation = itemView.findViewById(R.id.tvEventLocation);
        }
    }
}
