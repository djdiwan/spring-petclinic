package org.springframework.samples.petclinic.appointment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link AppointmentController}
 */
@WebMvcTest(AppointmentController.class)
class AppointmentControllerTests {

	private static final int TEST_PET_ID = 1;

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_VET_ID = 1;

	private static final int TEST_APPOINTMENT_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AppointmentService appointmentService;

	@MockBean
	private AvailabilityService availabilityService;

	@MockBean
	private VetRepository vetRepository;

	@MockBean
	private PetRepository petRepository;

	private Pet testPet;

	private Owner testOwner;

	private Vet testVet;

	private Appointment testAppointment;

	@BeforeEach
	void setup() {
		testOwner = new Owner();
		testOwner.setId(TEST_OWNER_ID);
		testOwner.setFirstName("George");
		testOwner.setLastName("Franklin");

		testPet = new Pet();
		testPet.setId(TEST_PET_ID);
		testPet.setName("Leo");
		testOwner.addPet(testPet);

		testVet = new Vet();
		testVet.setId(TEST_VET_ID);
		testVet.setFirstName("James");
		testVet.setLastName("Carter");

		testAppointment = new Appointment();
		testAppointment.setId(TEST_APPOINTMENT_ID);
		testAppointment.setPet(testPet);
		testAppointment.setVet(testVet);
		testAppointment.setAppointmentDate(LocalDate.now().plusDays(7));
		testAppointment.setStartTime(LocalTime.of(10, 0));
		testAppointment.setEndTime(LocalTime.of(10, 30));
		testAppointment.setDurationMinutes(30);
		testAppointment.setStatus(AppointmentStatus.CONFIRMED);
		testAppointment.setAppointmentType(AppointmentType.CHECKUP);

		given(this.petRepository.findById(TEST_PET_ID)).willReturn(testPet);
		given(this.vetRepository.findAll()).willReturn(Collections.singletonList(testVet));
		given(this.appointmentService.findById(TEST_APPOINTMENT_ID)).willReturn(testAppointment);
		given(this.appointmentService.findByDate(any(LocalDate.class))).willReturn(Collections.emptyList());
	}

	@Test
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/appointments/new", TEST_OWNER_ID, TEST_PET_ID))
				.andExpect(status().isOk()).andExpect(view().name("appointments/createAppointmentForm"))
				.andExpect(model().attributeExists("appointment")).andExpect(model().attributeExists("pet"))
				.andExpect(model().attributeExists("vets"));
	}

	@Test
	void testProcessCreationFormSuccess() throws Exception {
		given(this.appointmentService.createAppointment(any(Appointment.class))).willReturn(null);

		mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/appointments/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("appointmentDate", LocalDate.now().plusDays(7).toString()).param("startTime", "10:00")
				.param("appointmentType", "CHECKUP").param("description", "Annual checkup"))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	void testProcessCreationFormWithSchedulingError() throws Exception {
		given(this.appointmentService.createAppointment(any(Appointment.class)))
				.willReturn("The selected time slot is not available");

		mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/appointments/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("appointmentDate", LocalDate.now().plusDays(7).toString()).param("startTime", "10:00")
				.param("appointmentType", "CHECKUP")).andExpect(status().isOk())
				.andExpect(view().name("appointments/createAppointmentForm"));
	}

	@Test
	void testShowAppointment() throws Exception {
		mockMvc.perform(get("/appointments/{appointmentId}", TEST_APPOINTMENT_ID)).andExpect(status().isOk())
				.andExpect(view().name("appointments/appointmentDetails"))
				.andExpect(model().attributeExists("appointment"));
	}

	@Test
	void testUpdateStatusSuccess() throws Exception {
		given(this.appointmentService.updateStatus(eq(TEST_APPOINTMENT_ID), eq(AppointmentStatus.CHECKED_IN), any(),
				any())).willReturn(null);

		mockMvc.perform(
				post("/appointments/{appointmentId}/status", TEST_APPOINTMENT_ID).param("newStatus", "CHECKED_IN"))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	void testCancelAppointment() throws Exception {
		given(this.appointmentService.updateStatus(eq(TEST_APPOINTMENT_ID), eq(AppointmentStatus.CANCELLED), any(),
				eq("Owner requested cancellation"))).willReturn(null);

		mockMvc.perform(post("/appointments/{appointmentId}/cancel", TEST_APPOINTMENT_ID).param("cancelledReason",
				"Owner requested cancellation")).andExpect(status().is3xxRedirection());
	}

	@Test
	void testListAppointments() throws Exception {
		mockMvc.perform(get("/appointments")).andExpect(status().isOk())
				.andExpect(view().name("appointments/appointmentsList"))
				.andExpect(model().attributeExists("appointments"));
	}

	@Test
	void testDailySchedule() throws Exception {
		mockMvc.perform(get("/appointments/schedule")).andExpect(status().isOk())
				.andExpect(view().name("appointments/dailySchedule"));
	}

}
