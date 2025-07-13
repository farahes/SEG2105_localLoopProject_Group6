package com.example.localloopapp_android.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.localloopapp_android.datastores.RegistrationRepository;
import com.example.localloopapp_android.models.Registration;
import java.util.List;

public class RegistrationViewModel extends ViewModel {

    private RegistrationRepository repository;
    private LiveData<List<Registration>> pendingRegistrations;
    private LiveData<List<Registration>> participantRegistrations;

    public RegistrationViewModel() {
        repository = new RegistrationRepository();
    }

    public void loadPendingRegistrations(String organizerId) {
        pendingRegistrations = repository.getPendingRegistrationsForOrganizer(organizerId);
    }

    public LiveData<List<Registration>> getPendingRegistrations() {
        return pendingRegistrations;
    }

    public void approveRegistration(String registrationId) {
        repository.updateRegistrationStatus(registrationId, "approved");
    }

    public void rejectRegistration(String registrationId) {
        repository.updateRegistrationStatus(registrationId, "rejected");
    }

    public void loadParticipantRegistrations(String participantId) {
        participantRegistrations = repository.getRegistrationsForParticipant(participantId);
    }

    public LiveData<List<Registration>> getParticipantRegistrations() {
        return participantRegistrations;
    }
}
