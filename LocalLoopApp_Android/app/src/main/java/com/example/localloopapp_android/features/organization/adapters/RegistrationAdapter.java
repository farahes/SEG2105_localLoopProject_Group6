package com.example.localloopapp_android.features.organization.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.datastores.EventRepository;
import com.example.localloopapp_android.datastores.UserRepository;
import com.example.localloopapp_android.models.Event;
import com.example.localloopapp_android.models.Registration;
import com.example.localloopapp_android.models.accounts.UserAccount;

import java.util.List;

public class RegistrationAdapter extends RecyclerView.Adapter<RegistrationAdapter.RegistrationViewHolder> {

    private List<Registration> registrations;
    private OnRegistrationActionListener listener;
    private UserRepository userRepository;
    private EventRepository eventRepository;

    public interface OnRegistrationActionListener {
        void onAccept(Registration registration);
        void onReject(Registration registration);
    }

    public RegistrationAdapter(List<Registration> registrations, OnRegistrationActionListener listener) {
        this.registrations = registrations;
        this.listener = listener;
        this.userRepository = new UserRepository();
        this.eventRepository = new EventRepository();
    }

    @NonNull
    @Override
    public RegistrationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registration_row, parent, false);
        return new RegistrationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistrationViewHolder holder, int position) {
        Registration registration = registrations.get(position);
        holder.bind(registration, listener, userRepository, eventRepository);
    }

    @Override
    public int getItemCount() {
        return registrations.size();
    }

    public void updateRegistrations(List<Registration> newRegistrations) {
        this.registrations.clear();
        this.registrations.addAll(newRegistrations);
        notifyDataSetChanged();
    }

    static class RegistrationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvEventName;
        private TextView tvParticipantName;
        private Button btnAccept;
        private Button btnReject;

        public RegistrationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvParticipantName = itemView.findViewById(R.id.tvParticipantName);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        public void bind(final Registration registration, final OnRegistrationActionListener listener,
                     UserRepository userRepository, EventRepository eventRepository) {
            
            // Fetch Event Details
            eventRepository.getEvent(registration.getEventId(), new EventRepository.EventCallback() {
                @Override
                public void onSuccess(List<Event> events) {
                    if (!events.isEmpty()) {
                        tvEventName.setText(events.get(0).getName());
                    } else {
                        tvEventName.setText("Event not found");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    tvEventName.setText("Error loading event");
                }
            });

            // Fetch Participant Details
            userRepository.getUser(registration.getParticipantId(), new UserRepository.UserCallback() {
                @Override
                public void onSuccess(UserAccount user) {
                    if (user != null) {
                        tvParticipantName.setText(user.getFirstName() + " " + user.getLastName());
                    } else {
                        tvParticipantName.setText("Participant not found");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    tvParticipantName.setText("Error loading participant");
                }
            });

            btnAccept.setOnClickListener(v -> listener.onAccept(registration));
            btnReject.setOnClickListener(v -> listener.onReject(registration));
        }
    }
}
