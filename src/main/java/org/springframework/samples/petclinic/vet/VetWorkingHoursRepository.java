package org.springframework.samples.petclinic.vet;

import java.util.List;

import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for {@link VetWorkingHours} domain objects.
 */
public interface VetWorkingHoursRepository extends Repository<VetWorkingHours, Integer> {

	@Transactional(readOnly = true)
	List<VetWorkingHours> findByVetId(Integer vetId);

	void save(VetWorkingHours workingHours);

	void deleteById(Integer id);

}
