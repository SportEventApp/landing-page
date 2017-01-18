package org.fmarin.tournoidevincennes.repository;

import org.fmarin.tournoidevincennes.domain.Prospect;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Prospect entity.
 */
@SuppressWarnings("unused")
public interface ProspectRepository extends JpaRepository<Prospect,Long> {

}
