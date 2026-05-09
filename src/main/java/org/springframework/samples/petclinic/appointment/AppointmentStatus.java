package org.springframework.samples.petclinic.appointment;

/**
 * Enum representing the lifecycle states of an appointment.
 */
public enum AppointmentStatus {

	REQUESTED, CONFIRMED, CHECKED_IN, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW

}
