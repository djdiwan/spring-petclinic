package org.springframework.samples.petclinic.appointment;

import java.time.LocalTime;

/**
 * Represents an available time slot for booking.
 */
public class TimeSlot {

	private final LocalTime startTime;

	private final LocalTime endTime;

	public TimeSlot(LocalTime startTime, LocalTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public LocalTime getStartTime() {
		return this.startTime;
	}

	public LocalTime getEndTime() {
		return this.endTime;
	}

	public String getDisplayTime() {
		return this.startTime.toString() + " - " + this.endTime.toString();
	}

}
