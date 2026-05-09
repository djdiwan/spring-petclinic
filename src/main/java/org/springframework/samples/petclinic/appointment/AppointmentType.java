package org.springframework.samples.petclinic.appointment;

/**
 * Enum representing the types of appointments with default durations.
 */
public enum AppointmentType {

	CHECKUP(30), VACCINATION(15), SURGERY(60), DENTAL(45), EMERGENCY(30), GROOMING(45), FOLLOW_UP(15), OTHER(30);

	private final int defaultDurationMinutes;

	AppointmentType(int defaultDurationMinutes) {
		this.defaultDurationMinutes = defaultDurationMinutes;
	}

	public int getDefaultDurationMinutes() {
		return defaultDurationMinutes;
	}

}
