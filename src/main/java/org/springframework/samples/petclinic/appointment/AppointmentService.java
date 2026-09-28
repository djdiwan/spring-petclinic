package org.springframework.samples.petclinic.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling appointment business logic including scheduling rules, status
 * transitions, and conflict detection.
 */
@Service
public class AppointmentService {

	private static final int MAX_ADVANCE_DAYS = 90;

	private static final int MIN_LEAD_TIME_MINUTES = 60;

	private static final Set<AppointmentStatus> ACTIVE_STATUSES = EnumSet.of(AppointmentStatus.CONFIRMED,
			AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS);

	private final AppointmentRepository appointmentRepository;

	private final AvailabilityService availabilityService;

	public AppointmentService(AppointmentRepository appointmentRepository, AvailabilityService availabilityService) {
		this.appointmentRepository = appointmentRepository;
		this.availabilityService = availabilityService;
	}

	@Transactional(readOnly = true)
	public Appointment findById(Integer id) {
		return this.appointmentRepository.findById(id);
	}

	@Transactional(readOnly = true)
	public List<Appointment> findByDate(LocalDate date) {
		return this.appointmentRepository.findByDate(date);
	}

	@Transactional(readOnly = true)
	public List<Appointment> findActiveByVetAndDate(Integer vetId, LocalDate date) {
		return this.appointmentRepository.findActiveByVetAndDate(vetId, date);
	}

	@Transactional(readOnly = true)
	public List<Appointment> findByPetId(Integer petId) {
		return this.appointmentRepository.findByPetId(petId);
	}

	@Transactional(readOnly = true)
	public Collection<Appointment> findAll() {
		return this.appointmentRepository.findAll();
	}

	@Transactional
	public String createAppointment(Appointment appointment) {
		String validationError = validateNewAppointment(appointment);
		if (validationError != null) {
			return validationError;
		}

		appointment.setEndTime(appointment.getStartTime().plusMinutes(appointment.getDurationMinutes()));
		appointment.setStatus(AppointmentStatus.CONFIRMED);
		appointment.setCreatedAt(LocalDateTime.now());
		appointment.setUpdatedAt(LocalDateTime.now());
		this.appointmentRepository.save(appointment);
		return null;
	}

	@Transactional
	public String reschedule(Integer appointmentId, LocalDate newDate, LocalTime newStartTime) {
		Appointment appointment = this.appointmentRepository.findById(appointmentId);
		if (appointment == null) {
			return "Appointment not found";
		}
		if (appointment.getStatus() != AppointmentStatus.REQUESTED
				&& appointment.getStatus() != AppointmentStatus.CONFIRMED) {
			return "Only REQUESTED or CONFIRMED appointments can be rescheduled";
		}

		appointment.setAppointmentDate(newDate);
		appointment.setStartTime(newStartTime);
		appointment.setEndTime(newStartTime.plusMinutes(appointment.getDurationMinutes()));

		String validationError = validateNewAppointment(appointment);
		if (validationError != null) {
			return validationError;
		}

		appointment.setUpdatedAt(LocalDateTime.now());
		this.appointmentRepository.save(appointment);
		return null;
	}

	@Transactional
	public String updateStatus(Integer appointmentId, AppointmentStatus newStatus, String visitNotes,
			String cancelledReason) {
		Appointment appointment = this.appointmentRepository.findById(appointmentId);
		if (appointment == null) {
			return "Appointment not found";
		}

		String transitionError = validateStatusTransition(appointment.getStatus(), newStatus);
		if (transitionError != null) {
			return transitionError;
		}

		if (newStatus == AppointmentStatus.COMPLETED && (visitNotes == null || visitNotes.trim().isEmpty())) {
			return "Visit notes are required when completing an appointment";
		}
		if (newStatus == AppointmentStatus.CANCELLED && (cancelledReason == null || cancelledReason.trim().isEmpty())) {
			return "Cancellation reason is required";
		}

		appointment.setStatus(newStatus);
		if (visitNotes != null && !visitNotes.trim().isEmpty()) {
			appointment.setVisitNotes(visitNotes);
		}
		if (cancelledReason != null && !cancelledReason.trim().isEmpty()) {
			appointment.setCancelledReason(cancelledReason);
		}
		appointment.setUpdatedAt(LocalDateTime.now());
		this.appointmentRepository.save(appointment);
		return null;
	}

	private String validateNewAppointment(Appointment appointment) {
		LocalDate date = appointment.getAppointmentDate();
		LocalTime startTime = appointment.getStartTime();

		if (date == null || startTime == null) {
			return "Date and start time are required";
		}

		if (appointment.getVet() == null) {
			return "A veterinarian must be selected";
		}

		if (appointment.getPet() == null) {
			return "A pet must be selected";
		}

		// R4: Future dates only (emergency can be today)
		LocalDate today = LocalDate.now();
		if (date.isBefore(today)) {
			return "Appointments must be scheduled for a future date";
		}

		// R5: Minimum lead time (non-emergency)
		if (appointment.getAppointmentType() != AppointmentType.EMERGENCY) {
			LocalDateTime appointmentDateTime = LocalDateTime.of(date, startTime);
			if (appointmentDateTime.isBefore(LocalDateTime.now().plusMinutes(MIN_LEAD_TIME_MINUTES))) {
				return "Non-emergency appointments must be booked at least 1 hour in advance";
			}
		}

		// R6: Maximum advance booking
		if (date.isAfter(today.plusDays(MAX_ADVANCE_DAYS))) {
			return "Appointments cannot be booked more than 90 days in advance";
		}

		// R7: 15-minute interval alignment
		if (startTime.getMinute() % 15 != 0) {
			return "Appointment start times must align to 15-minute intervals";
		}

		// R1, R2, R3: Check availability
		int duration = appointment.getDurationMinutes() != null ? appointment.getDurationMinutes()
				: appointment.getAppointmentType().getDefaultDurationMinutes();
		List<TimeSlot> availableSlots = availabilityService.getAvailableSlots(appointment.getVet().getId(), date,
				duration);

		boolean slotAvailable = availableSlots.stream().anyMatch(slot -> slot.getStartTime().equals(startTime));

		if (!slotAvailable) {
			return "The selected time slot is not available. The vet may not be working, "
					+ "the slot may conflict with an existing appointment, or it falls during a blocked period.";
		}

		return null;
	}

	private String validateStatusTransition(AppointmentStatus currentStatus, AppointmentStatus newStatus) {
		switch (currentStatus) {
		case REQUESTED:
			if (newStatus != AppointmentStatus.CONFIRMED && newStatus != AppointmentStatus.CANCELLED) {
				return "REQUESTED appointments can only be CONFIRMED or CANCELLED";
			}
			break;
		case CONFIRMED:
			if (newStatus != AppointmentStatus.CHECKED_IN && newStatus != AppointmentStatus.CANCELLED
					&& newStatus != AppointmentStatus.NO_SHOW) {
				return "CONFIRMED appointments can only be CHECKED_IN, CANCELLED, or NO_SHOW";
			}
			break;
		case CHECKED_IN:
			if (newStatus != AppointmentStatus.IN_PROGRESS && newStatus != AppointmentStatus.CANCELLED) {
				return "CHECKED_IN appointments can only move to IN_PROGRESS or CANCELLED";
			}
			break;
		case IN_PROGRESS:
			if (newStatus != AppointmentStatus.COMPLETED) {
				return "IN_PROGRESS appointments can only be COMPLETED";
			}
			break;
		case COMPLETED:
		case CANCELLED:
		case NO_SHOW:
			return currentStatus.name() + " is a terminal state and cannot be changed";
		default:
			return "Unknown status: " + currentStatus;
		}
		return null;
	}

}
