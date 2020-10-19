package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Institution;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Enables the use of and access to the institution database where data on the banking institution is stored
 */
@Repository
public interface InstitutionRepository extends CrudRepository<Institution, Long> {


    Optional<Institution> getByInstitutionId(String institutionId);


}
