package ua.ukma.edu.domain;

import ua.ukma.edu.exceptions.AppointmentException;

import java.time.LocalDateTime;

public record Appointment(String id, Doctor doctor, Patient patient, TimeSlot timeSlot, Status status,
						  LocalDateTime createdAt, LocalDateTime updatedAt) {

	public Appointment {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Appointment id must not be blank.");
		}
		if (doctor == null) {
			throw new IllegalArgumentException("Doctor must not be null.");
		}
		if (patient == null) {
			throw new IllegalArgumentException("Patient must not be null.");
		}
		if (timeSlot == null) {
			throw new IllegalArgumentException("Time slot must not be null.");
		}
		status = status == null ? Status.CREATED : status;
		createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
		updatedAt = updatedAt == null ? createdAt : updatedAt;
	}

	public boolean isActive() {
		return status.isActive();
	}

	public Appointment confirm() {
		return transitionTo(Status.CONFIRMED);
	}

	public Appointment complete() {
		return transitionTo(Status.COMPLETED);
	}

	public Appointment cancel() {
		return transitionTo(Status.CANCELLED);
	}

	public Appointment reschedule(TimeSlot newSlot) {
		if (newSlot == null) {
			throw new IllegalArgumentException("New slot must not be null.");
		}
		return new Appointment(id, doctor, patient, newSlot, status, createdAt, LocalDateTime.now());
	}

	public Appointment transitionTo(Status nextStatus) {
		if (!status.canTransitionTo(nextStatus)) {
			throw new AppointmentException("Cannot transition appointment from %s to %s.".formatted(status, nextStatus));
		}
		return new Appointment(id, doctor, patient, timeSlot, nextStatus, createdAt, LocalDateTime.now());
	}
}
