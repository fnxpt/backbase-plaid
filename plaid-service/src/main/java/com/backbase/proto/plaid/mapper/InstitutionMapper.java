package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Institution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * InstitutionMapper:
 * Maps the fields and data from plaid response containing a institution to an institution object that can
 * be ingested by backbase
 */
@Mapper
public interface InstitutionMapper {
    /**
     * Maps the institution ID from the plaid response
     *
     * @param source the institution returned from plaid
     * @return the institution to be ingested by backbase
     */
    @Mapping(target = "id", ignore = true)
    Institution map(com.plaid.client.response.Institution source);
}
