package org.springframework.samples.petclinic.appointment;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for {@link Appointment} domain objects.
 */
public interface AppointmentRepository extends Repository<Appointment, Integer> {

	@Transactional(readOnly = true)
	Appointment findById(Integer id);

	@Transactional(readOnly = true)
	@Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.pet p LEFT JOIN FETCH p.owner LEFT JOIN FETCH a.vet "
			+ "WHERE a.vet.id = :vetId AND a.appointmentDate = :date "
			+ "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') ORDER BY a.startTime")
	List<Appointment> findActiveByVetAndDate(@Param("vetId") Integer vetId, @Param("date") LocalDate date);

	@Transactional(readOnly = true)
	@Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.pet p LEFT JOIN FETCH p.owner LEFT JOIN FETCH a.vet "
			+ "WHERE a.appointmentDate = :date ORDER BY a.startTime")
	List<Appointment> findByDate(@Param("date") LocalDate date);

	@Transactional(readOnly = true)
	@Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.pet p LEFT JOIN FETCH p.owner LEFT JOIN FETCH a.vet "
			+ "WHERE a.pet.id = :petId ORDER BY a.appointmentDate DESC, a.startTime DESC")
	List<Appointment> findByPetId(@Param("petId") Integer petId);

	@Transactional(readOnly = true)
	@Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.pet p LEFT JOIN FETCH p.owner LEFT JOIN FETCH a.vet "
			+ "ORDER BY a.appointmentDate DESC, a.startTime DESC")
	Collection<Appointment> findAll();

	void save(Appointment appointment);

}
