package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.mapper.InstitutionMapper;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.proto.plaid.repository.InstitutionRepository;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.InstitutionsGetByIdRequest;
import com.plaid.client.response.InstitutionsGetByIdResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionMapper institutionMapper = Mappers.getMapper(InstitutionMapper.class);

    private final InstitutionRepository institutionRepository;

    private final PlaidClient plaidClient;

    public Institution register(Institution institution) {
        return getInstitution(institution.getInstitutionId());
    }

    public Institution getInstitution(String institutionId) {
        return institutionRepository.getByInstitutionId(institutionId)
            .orElseGet(() -> {
                InstitutionsGetByIdResponse institutionsGetResponse = null;
                institutionsGetResponse = requestInstitution(institutionId);
                Institution institution = institutionMapper.map(institutionsGetResponse.getInstitution());
                return institutionRepository.save(institution);
            });
    }

    @NotNull
    private InstitutionsGetByIdResponse requestInstitution(String institutionId) {
        InstitutionsGetByIdResponse institutionsGetResponse;
        try {
            institutionsGetResponse = plaidClient.service()
                .institutionsGetById(new InstitutionsGetByIdRequest(institutionId))
                .execute()
                .body();
        } catch (IOException e) {
            throw new BadRequestException("Failed to get institution by Id: " + institutionId);
        }
        assert institutionsGetResponse != null;
        return institutionsGetResponse;
    }


}
