package org.springframework.samples.petclinic.appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Vet;

/**
 * Test class for {@link AppointmentService}
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTests {

	@Mock
	private AppointmentRepository appointmentRepository;

	@Mock
	private AvailabilityService availabilityService;

	@InjectMocks
	private AppointmentService appointmentService;

	private Appointment appointment;

	private Pet pet;

	private Vet vet;

	@BeforeEach
	void setup() {
		pet = new Pet();
		pet.setId(1);
		pet.setName("Leo");

		vet = new Vet();
		vet.setId(1);
		vet.setFirstName("James");
		vet.setLastName("Carter");

		appointment = new Appointment();
		appointment.setPet(pet);
		appointment.setVet(vet);
		appointment.setAppointmentDate(LocalDate.now().plusDays(7));
		appointment.setStartTime(LocalTime.of(10, 0));
		appointment.setDurationMinutes(30);
		appointment.setAppointmentType(AppointmentType.CHECKUP);
		appointment.setStatus(AppointmentStatus.CONFIRMED);
	}

	@Test
	void testCreateAppointmentSuccess() {
		given(availabilityService.getAvailableSlots(anyInt(), any(LocalDate.class), anyInt()))
				.willReturn(Collections.singletonList(new TimeSlot(LocalTime.of(10, 0), LocalTime.of(10, 30))));

		String error = appointmentService.createAppointment(appointment);
		assertThat(error).isNull();
		verify(appointmentRepository).save(appointment);
	}

	@Test
	void testCreateAppointmentInPast() {
		appointment.setAppointmentDate(LocalDate.now().minusDays(1));
		String error = appointmentService.createAppointment(appointment);
		assertThat(error).isEqualTo("Appointments must be scheduled for a future date");
	}

	@Test
	void testCreateAppointmentTooFarInFuture() {
		appointment.setAppointmentDate(LocalDate.now().plusDays(100));
		String error = appointmentService.createAppointment(appointment);
		assertThat(error).isEqualTo("Appointments cannot be booked more than 90 days in advance");
	}

	@Test
	void testCreateAppointmentBadTimeAlignment() {
		appointment.setStartTime(LocalTime.of(10, 7));
		String error = appointmentService.createAppointment(appointment);
		assertThat(error).isEqualTo("Appointment start times must align to 15-minute intervals");
	}

	@Test
	void testCreateAppointmentNoVet() {
		appointment.setVet(null);
		String error = appointmentService.createAppointment(appointment);
		assertThat(error).isEqualTo("A veterinarian must be selected");
	}

	@Test
	void testCreateAppointmentNoPet() {
		appointment.setPet(null);
		String error = appointmentService.createAppointment(appointment);
		assertThat(error).isEqualTo("A pet must be selected");
	}

	@Test
	void testCreateAppointmentSlotUnavailable() {
		given(availabilityService.getAvailableSlots(anyInt(), any(LocalDate.class), anyInt()))
				.willReturn(Collections.emptyList());

		String error = appointmentService.createAppointment(appointment);
		assertThat(error).startsWith("The selected time slot is not available");
	}

	@Test
	void testUpdateStatusCompletedWithoutNotes() {
		appointment.setId(1);
		appointment.setStatus(AppointmentStatus.IN_PROGRESS);
		given(appointmentRepository.findById(1)).willReturn(appointment);

		String error = appointmentService.updateStatus(1, AppointmentStatus.COMPLETED, null, null);
		assertThat(error).isEqualTo("Visit notes are required when completing an appointment");
	}

	@Test
	void testUpdateStatusCompletedWithNotes() {
		appointment.setId(1);
		appointment.setStatus(AppointmentStatus.IN_PROGRESS);
		given(appointmentRepository.findById(1)).willReturn(appointment);

		String error = appointmentService.updateStatus(1, AppointmentStatus.COMPLETED, "All good", null);
		assertThat(error).isNull();
		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
	}

	@Test
	void testUpdateStatusCancelledWithoutReason() {
		appointment.setId(1);
		appointment.setStatus(AppointmentStatus.CONFIRMED);
		given(appointmentRepository.findById(1)).willReturn(appointment);

		String error = appointmentService.updateStatus(1, AppointmentStatus.CANCELLED, null, null);
		assertThat(error).isEqualTo("Cancellation reason is required");
	}

	@Test
	void testUpdateStatusInvalidTransition() {
		appointment.setId(1);
		appointment.setStatus(AppointmentStatus.COMPLETED);
		given(appointmentRepository.findById(1)).willReturn(appointment);

		String error = appointmentService.updateStatus(1, AppointmentStatus.CONFIRMED, null, null);
		assertThat(error).isEqualTo("COMPLETED is a terminal state and cannot be changed");
	}

	@Test
	void testUpdateStatusConfirmedToCheckedIn() {
		appointment.setId(1);
		appointment.setStatus(AppointmentStatus.CONFIRMED);
		given(appointmentRepository.findById(1)).willReturn(appointment);

		String error = appointmentService.updateStatus(1, AppointmentStatus.CHECKED_IN, null, null);
		assertThat(error).isNull();
		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CHECKED_IN);
	}

}
