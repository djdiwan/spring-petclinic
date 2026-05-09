package org.springframework.samples.petclinic.appointment;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * {@link Validator} for {@link Appointment} forms.
 */
public class AppointmentValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Appointment.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Appointment appointment = (Appointment) target;

		if (appointment.getAppointmentDate() == null) {
			errors.rejectValue("appointmentDate", "required", "Date is required");
		}

		if (appointment.getStartTime() == null) {
			errors.rejectValue("startTime", "required", "Start time is required");
		}

		if (appointment.getAppointmentType() == null) {
			errors.rejectValue("appointmentType", "required", "Appointment type is required");
		}
	}

}
