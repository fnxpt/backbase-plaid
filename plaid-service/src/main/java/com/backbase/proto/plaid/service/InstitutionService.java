package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.mapper.InstitutionMapper;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.proto.plaid.repository.InstitutionRepository;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.InstitutionsGetByIdRequest;
import com.plaid.client.response.InstitutionsGetByIdResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

/**
 * allows the retrieval and ingestion of institution data when it is available from plaid
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionMapper institutionMapper = Mappers.getMapper(InstitutionMapper.class);

    private final InstitutionRepository institutionRepository;

    private final PlaidClient plaidClient;

    /**
     * gets the institution data from the repository if previously retrieved if it is not available from the
     * repository it Will be requested from plaid and stored
     * @param institutionId identifies the instituted the data is being retrieved for
     * @param userId ??
     * @return institution data
     */
    public Institution getInstitution(String institutionId, String userId) {
        return institutionRepository.getByInstitutionId(institutionId)
            .orElseGet(() -> {
                InstitutionsGetByIdResponse institutionsGetResponse = null;
                institutionsGetResponse = requestInstitution(institutionId);
                Institution institution = institutionMapper.map(institutionsGetResponse.getInstitution());
                institution.setFirstCreatedBy(userId);
                institution.setFirstRegisteredAt(LocalDateTime.now());
                return institutionRepository.save(institution);
            });
    }

    /**
     * builds and sends a request to plaid for the data of an institution
     * @param institutionId identifies the desired institution
     * @return the response from plaid
     */
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
