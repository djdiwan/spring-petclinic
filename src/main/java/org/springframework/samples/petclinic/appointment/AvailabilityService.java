package org.springframework.samples.petclinic.appointment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.samples.petclinic.vet.VetTimeBlock;
import org.springframework.samples.petclinic.vet.VetTimeBlockRepository;
import org.springframework.samples.petclinic.vet.VetWorkingHours;
import org.springframework.samples.petclinic.vet.VetWorkingHoursRepository;
import org.springframework.stereotype.Service;

/**
 * Service that calculates available appointment time slots for a vet on a given date.
 */
@Service
public class AvailabilityService {

	private static final int SLOT_INCREMENT_MINUTES = 15;

	private final AppointmentRepository appointmentRepository;

	private final VetWorkingHoursRepository workingHoursRepository;

	private final VetTimeBlockRepository timeBlockRepository;

	public AvailabilityService(AppointmentRepository appointmentRepository,
			VetWorkingHoursRepository workingHoursRepository, VetTimeBlockRepository timeBlockRepository) {
		this.appointmentRepository = appointmentRepository;
		this.workingHoursRepository = workingHoursRepository;
		this.timeBlockRepository = timeBlockRepository;
	}

	public List<TimeSlot> getAvailableSlots(Integer vetId, LocalDate date, int durationMinutes) {
		DayOfWeek dayOfWeek = date.getDayOfWeek();

		List<VetWorkingHours> workingHoursList = workingHoursRepository.findByVetId(vetId);
		VetWorkingHours workingHours = workingHoursList.stream().filter(wh -> wh.getDayOfWeek() == dayOfWeek)
				.findFirst().orElse(null);

		if (workingHours == null) {
			return new ArrayList<>();
		}

		List<Appointment> existingAppointments = appointmentRepository.findActiveByVetAndDate(vetId, date);
		List<VetTimeBlock> timeBlocks = timeBlockRepository.findByVetIdAndBlockDate(vetId, date);

		List<LocalTime[]> busyIntervals = new ArrayList<>();
		for (Appointment appt : existingAppointments) {
			busyIntervals.add(new LocalTime[] { appt.getStartTime(), appt.getEndTime() });
		}
		for (VetTimeBlock block : timeBlocks) {
			busyIntervals.add(new LocalTime[] { block.getStartTime(), block.getEndTime() });
		}
		busyIntervals.sort(Comparator.comparing(interval -> interval[0]));

		List<TimeSlot> slots = new ArrayList<>();
		LocalTime cursor = workingHours.getStartTime();
		LocalTime workEnd = workingHours.getEndTime();

		while (!cursor.plusMinutes(durationMinutes).isAfter(workEnd)) {
			LocalTime candidateEnd = cursor.plusMinutes(durationMinutes);
			if (!overlapsAny(cursor, candidateEnd, busyIntervals)) {
				slots.add(new TimeSlot(cursor, candidateEnd));
			}
			cursor = cursor.plusMinutes(SLOT_INCREMENT_MINUTES);
		}

		return slots;
	}

	private boolean overlapsAny(LocalTime start, LocalTime end, List<LocalTime[]> intervals) {
		for (LocalTime[] interval : intervals) {
			if (start.isBefore(interval[1]) && end.isAfter(interval[0])) {
				return true;
			}
		}
		return false;
	}

}
