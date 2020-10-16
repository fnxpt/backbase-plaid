package com.backbase.proto.plaid.mapper;

import com.backbase.proto.plaid.model.Institution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface InstitutionMapper {

    @Mapping(target = "id", ignore = true)
    Institution map(com.plaid.client.response.Institution source);
}
