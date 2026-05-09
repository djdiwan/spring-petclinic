package org.springframework.samples.petclinic.vet;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for {@link VetTimeBlock} domain objects.
 */
public interface VetTimeBlockRepository extends Repository<VetTimeBlock, Integer> {

	@Transactional(readOnly = true)
	List<VetTimeBlock> findByVetIdAndBlockDate(Integer vetId, LocalDate blockDate);

	@Transactional(readOnly = true)
	List<VetTimeBlock> findByVetId(Integer vetId);

	void save(VetTimeBlock timeBlock);

	void deleteById(Integer id);

}
