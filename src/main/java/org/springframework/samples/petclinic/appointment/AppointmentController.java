package org.springframework.samples.petclinic.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for appointment scheduling.
 */
@Controller
class AppointmentController {

	private final AppointmentService appointmentService;

	private final AvailabilityService availabilityService;

	private final VetRepository vetRepository;

	private final PetRepository petRepository;

	AppointmentController(AppointmentService appointmentService, AvailabilityService availabilityService,
			VetRepository vetRepository, PetRepository petRepository) {
		this.appointmentService = appointmentService;
		this.availabilityService = availabilityService;
		this.vetRepository = vetRepository;
		this.petRepository = petRepository;
	}

	@InitBinder("appointment")
	public void initBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new AppointmentValidator());
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}/appointments/new")
	public String initCreationForm(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			Model model) {
		Pet pet = this.petRepository.findById(petId);
		Appointment appointment = new Appointment();
		appointment.setPet(pet);
		model.addAttribute("appointment", appointment);
		model.addAttribute("pet", pet);
		model.addAttribute("vets", this.vetRepository.findAll());
		model.addAttribute("appointmentTypes", AppointmentType.values());
		return "appointments/createAppointmentForm";
	}

	@PostMapping("/owners/{ownerId}/pets/{petId}/appointments/new")
	public String processCreationForm(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			@ModelAttribute Appointment appointment, BindingResult result, @RequestParam("vetId") int vetId,
			RedirectAttributes redirectAttributes, Model model) {

		Pet pet = this.petRepository.findById(petId);
		appointment.setPet(pet);

		Collection<Vet> allVets = this.vetRepository.findAll();
		Vet vet = allVets.stream().filter(v -> v.getId().equals(vetId)).findFirst().orElse(null);
		appointment.setVet(vet);

		if (appointment.getAppointmentType() != null) {
			appointment.setDurationMinutes(appointment.getAppointmentType().getDefaultDurationMinutes());
		}

		new AppointmentValidator().validate(appointment, result);
		if (result.hasErrors()) {
			model.addAttribute("pet", pet);
			model.addAttribute("vets", allVets);
			model.addAttribute("appointmentTypes", AppointmentType.values());
			return "appointments/createAppointmentForm";
		}

		String error = this.appointmentService.createAppointment(appointment);
		if (error != null) {
			result.reject("scheduling.error", error);
			model.addAttribute("pet", pet);
			model.addAttribute("vets", allVets);
			model.addAttribute("appointmentTypes", AppointmentType.values());
			return "appointments/createAppointmentForm";
		}

		redirectAttributes.addFlashAttribute("message", "Appointment booked successfully!");
		return "redirect:/appointments/" + appointment.getId();
	}

	@GetMapping("/appointments/{appointmentId}")
	public String showAppointment(@PathVariable("appointmentId") int appointmentId, Model model) {
		Appointment appointment = this.appointmentService.findById(appointmentId);
		model.addAttribute("appointment", appointment);
		model.addAttribute("statuses", AppointmentStatus.values());
		return "appointments/appointmentDetails";
	}

	@PostMapping("/appointments/{appointmentId}/status")
	public String updateStatus(@PathVariable("appointmentId") int appointmentId,
			@RequestParam("newStatus") AppointmentStatus newStatus,
			@RequestParam(value = "visitNotes", required = false) String visitNotes,
			@RequestParam(value = "cancelledReason", required = false) String cancelledReason,
			RedirectAttributes redirectAttributes) {

		String error = this.appointmentService.updateStatus(appointmentId, newStatus, visitNotes, cancelledReason);
		if (error != null) {
			redirectAttributes.addFlashAttribute("error", error);
		}
		else {
			redirectAttributes.addFlashAttribute("message", "Status updated to " + newStatus);
		}
		return "redirect:/appointments/" + appointmentId;
	}

	@PostMapping("/appointments/{appointmentId}/cancel")
	public String cancelAppointment(@PathVariable("appointmentId") int appointmentId,
			@RequestParam("cancelledReason") String cancelledReason, RedirectAttributes redirectAttributes) {
		String error = this.appointmentService.updateStatus(appointmentId, AppointmentStatus.CANCELLED, null,
				cancelledReason);
		if (error != null) {
			redirectAttributes.addFlashAttribute("error", error);
		}
		else {
			redirectAttributes.addFlashAttribute("message", "Appointment cancelled");
		}
		return "redirect:/appointments/" + appointmentId;
	}

	@PostMapping("/appointments/{appointmentId}/reschedule")
	public String rescheduleAppointment(@PathVariable("appointmentId") int appointmentId,
			@RequestParam("newDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate newDate,
			@RequestParam("newStartTime") @DateTimeFormat(pattern = "HH:mm") LocalTime newStartTime,
			RedirectAttributes redirectAttributes) {
		String error = this.appointmentService.reschedule(appointmentId, newDate, newStartTime);
		if (error != null) {
			redirectAttributes.addFlashAttribute("error", error);
		}
		else {
			redirectAttributes.addFlashAttribute("message", "Appointment rescheduled successfully");
		}
		return "redirect:/appointments/" + appointmentId;
	}

	@GetMapping("/appointments")
	public String listAppointments(
			@RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
			Model model) {
		if (date == null) {
			date = LocalDate.now();
		}
		List<Appointment> appointments = this.appointmentService.findByDate(date);
		model.addAttribute("appointments", appointments);
		model.addAttribute("selectedDate", date);
		model.addAttribute("vets", this.vetRepository.findAll());
		return "appointments/appointmentsList";
	}

	@GetMapping("/appointments/schedule")
	public String dailySchedule(
			@RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
			@RequestParam(value = "vetId", required = false) Integer vetId, Model model) {
		if (date == null) {
			date = LocalDate.now();
		}
		Collection<Vet> allVets = this.vetRepository.findAll();
		model.addAttribute("vets", allVets);
		model.addAttribute("selectedDate", date);
		model.addAttribute("selectedVetId", vetId);

		if (vetId != null) {
			List<Appointment> appointments = this.appointmentService.findActiveByVetAndDate(vetId, date);
			model.addAttribute("appointments", appointments);
		}
		return "appointments/dailySchedule";
	}

	@GetMapping("/api/vets/{vetId}/available-slots")
	@ResponseBody
	public List<TimeSlot> getAvailableSlots(@PathVariable("vetId") int vetId,
			@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
			@RequestParam(value = "type", defaultValue = "CHECKUP") AppointmentType type) {
		return this.availabilityService.getAvailableSlots(vetId, date, type.getDefaultDurationMinutes());
	}

}
