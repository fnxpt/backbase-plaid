package com.backbase.proto.plaid.repository;

import com.backbase.proto.plaid.model.Institution;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * This class enables the use of and access to the institution database where data on the banking institution is stored.
 */
@Repository
public interface InstitutionRepository extends CrudRepository<Institution, Long> {

    /**
     * Finds an institution record in the Institution table by its ID.
     *
     * @param institutionId identifies the institution being search for
     * @return institution object with the ID parsed if found otherwise nothing is returned
     */
    Optional<Institution> getByInstitutionId(String institutionId);




}
