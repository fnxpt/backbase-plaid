package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Institution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * This class maps the fields and data from Plaid response containing a institution to an institution object that can
 * be ingested by Backbase.
 */
@Mapper
public interface InstitutionMapper {
    /**
     * Maps the institution ID from the Plaid response.
     *
     * @param source the institution returned from Plaid
     * @return the institution to be ingested by Backbase
     */
    @Mapping(target = "id", ignore = true)
    Institution map(com.plaid.client.response.Institution source);
}
