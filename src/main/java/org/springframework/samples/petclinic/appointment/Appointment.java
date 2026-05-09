package org.springframework.samples.petclinic.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Vet;

/**
 * Simple JavaBean domain object representing an appointment.
 */
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "pet_id")
	private Pet pet;

	@ManyToOne
	@JoinColumn(name = "vet_id")
	private Vet vet;

	@Column(name = "appointment_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate appointmentDate;

	@Column(name = "start_time")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime startTime;

	@Column(name = "end_time")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime endTime;

	@Column(name = "duration_minutes")
	private Integer durationMinutes;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private AppointmentStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "appointment_type")
	private AppointmentType appointmentType;

	@Column(name = "description")
	private String description;

	@Column(name = "visit_notes")
	private String visitNotes;

	@Column(name = "cancelled_reason")
	private String cancelledReason;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public Appointment() {
		this.status = AppointmentStatus.CONFIRMED;
		this.appointmentType = AppointmentType.CHECKUP;
		this.durationMinutes = AppointmentType.CHECKUP.getDefaultDurationMinutes();
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public Pet getPet() {
		return this.pet;
	}

	public void setPet(Pet pet) {
		this.pet = pet;
	}

	public Vet getVet() {
		return this.vet;
	}

	public void setVet(Vet vet) {
		this.vet = vet;
	}

	public LocalDate getAppointmentDate() {
		return this.appointmentDate;
	}

	public void setAppointmentDate(LocalDate appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public LocalTime getStartTime() {
		return this.startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return this.endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public Integer getDurationMinutes() {
		return this.durationMinutes;
	}

	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public AppointmentStatus getStatus() {
		return this.status;
	}

	public void setStatus(AppointmentStatus status) {
		this.status = status;
	}

	public AppointmentType getAppointmentType() {
		return this.appointmentType;
	}

	public void setAppointmentType(AppointmentType appointmentType) {
		this.appointmentType = appointmentType;
		if (appointmentType != null) {
			this.durationMinutes = appointmentType.getDefaultDurationMinutes();
		}
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVisitNotes() {
		return this.visitNotes;
	}

	public void setVisitNotes(String visitNotes) {
		this.visitNotes = visitNotes;
	}

	public String getCancelledReason() {
		return this.cancelledReason;
	}

	public void setCancelledReason(String cancelledReason) {
		this.cancelledReason = cancelledReason;
	}

	public LocalDateTime getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

}
