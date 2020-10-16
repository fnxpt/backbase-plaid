package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.mapper.AccountMapper;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.proto.plaid.repository.AccountRepository;
import com.backbase.stream.configuration.AccessControlConfiguration;
import com.backbase.stream.legalentity.model.JobProfileUser;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.legalentity.model.LegalEntityReference;
import com.backbase.stream.legalentity.model.ProductGroup;
import com.backbase.stream.legalentity.model.ServiceAgreement;
import com.backbase.stream.legalentity.model.User;
import com.backbase.stream.product.BatchProductIngestionSaga;
import com.backbase.stream.product.ProductIngestionSagaConfiguration;
import com.backbase.stream.product.task.ProductGroupTask;
import com.backbase.stream.productcatalog.ProductCatalogService;
import com.backbase.stream.productcatalog.configuration.ProductCatalogServiceConfiguration;
import com.backbase.stream.productcatalog.model.ProductCatalog;
import com.backbase.stream.productcatalog.model.ProductKind;
import com.backbase.stream.productcatalog.model.ProductType;
import com.backbase.stream.service.LegalEntityService;
import com.backbase.stream.worker.exception.StreamTaskException;
import com.backbase.stream.worker.model.StreamTask;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.AccountsBalanceGetRequest;
import com.plaid.client.response.Account;
import com.plaid.client.response.AccountsBalanceGetResponse;
import com.plaid.client.response.ItemStatus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Import({
    ProductIngestionSagaConfiguration.class,
    AccessControlConfiguration.class,
    ProductCatalogServiceConfiguration.class,

})
public class AccountService {

    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    private final PlaidClient plaidClient;
    private final PlaidConfigurationProperties plaidConfigurationProperties;

    private final AccountRepository accountRepository;

    private final InstitutionService institutionService;

    private final LegalEntityService legalEntityService;

    private final BatchProductIngestionSaga batchProductIngestionSaga;

    private final ProductCatalogService productCatalogService;

    public AccountsBalanceGetResponse requestPlaidAccounts(String accessToken) {
        try {
            return plaidClient.service().accountsBalanceGet(new AccountsBalanceGetRequest(accessToken)).execute().body();
        } catch (IOException e) {
            throw new BadRequestException("Failed to get access token");
        }
    }

    public void ingestPlaidAccounts(String accessToken, String userId, String legalEntityId) {
        AccountsBalanceGetResponse plaidAccounts = requestPlaidAccounts(accessToken);
        List<Account> accounts = plaidAccounts.getAccounts();

        setupProductCatalog(accounts);

        accountRepository.saveAll(accounts.stream()
            .map(account -> accountMapper.mapToDomain(account).withItemId(plaidAccounts.getItem().getItemId()))
            .collect(Collectors.toList()));

        ItemStatus itemStatus = plaidAccounts.getItem();
        String institutionId = itemStatus.getInstitutionId();
        Institution institution = institutionService.getInstitution(institutionId);

        LegalEntity legalEntityByInternalId = legalEntityService.getLegalEntityByInternalId(legalEntityId)
            .blockOptional()
            .orElseThrow(() -> new BadRequestException("Legal Entity does not exist"));

        JobProfileUser jobProfileUser = new JobProfileUser();
        jobProfileUser.setReferenceJobRoleNames(plaidConfigurationProperties.getDefaultReferenceJobRoleNames());
        jobProfileUser.setLegalEntityReference(new LegalEntityReference()
            .internalId(legalEntityId)
            .externalId(legalEntityByInternalId.getExternalId()));
        jobProfileUser.setUser(new User().externalId(userId));

        ServiceAgreement serviceAgreement = legalEntityService.getMasterServiceAgreementForInternalLegalEntityId(legalEntityId)
            .blockOptional()
            .orElseThrow(() -> new BadRequestException("Legal Entity does not have a valid service agreement"));

        ProductGroup productGroup = new ProductGroup();
        productGroup.setServiceAgreement(serviceAgreement);
        productGroup.setName("Linked Plaid Products");
        productGroup.setDescription("External Products Linked with Plaid");
        productGroup.addUsersItem(jobProfileUser);
        productGroup.setCustomProducts(accounts.stream()
            .map(account -> accountMapper.mapToStream(accessToken, itemStatus, institution, account)).collect(Collectors.toList()));

        batchProductIngestionSaga.process(new ProductGroupTask().data(productGroup))
            .doOnNext(StreamTask::logSummary)
            .doOnError(StreamTaskException.class, e -> {
                log.error("Failed ot setup Product Group: {}", e.getMessage(), e);
                e.getTask().logSummary();
            })
            .block();

    }

    /**
     * Ensure that all types and sub types from Plaid exists in Backbase DBS Product Catalog
     *
     * @param plaidAccounts List of linked Plaid Accounts
     */
    public void setupProductCatalog(List<Account> plaidAccounts) {
        ProductCatalog productCatalog = new ProductCatalog();
        productCatalog.setProductTypes(new ArrayList<>());
        plaidAccounts.stream()
            .collect(Collectors.groupingBy(Account::getType))
            .forEach((type, accounts) -> {
                String kindId = accountMapper.mapProductType(type, "external-");
                ProductKind productKindsItem = new ProductKind()
                    .externalKindId(kindId)
                    .kindUri(kindId)
                    .kindName("External " + StringUtils.capitalize(type));
                productCatalog.addProductKindsItem(productKindsItem);
                productCatalog.getProductTypes().addAll((accounts.stream()
                    .map(Account::getSubtype).collect(Collectors.toSet())
                    .stream().map(subtype -> {
                        String productTypeId = accountMapper.mapSubTypeId(subtype);
                        return new ProductType()
                            .externalId(productTypeId)
                            .externalProductId(productTypeId)
                            .externalProductKindId(kindId)
                            .productTypeName(StringUtils.capitalize(subtype));
                    })
                    .collect(Collectors.toList())));
            });
        log.info("Setting up Product Catalog with: {}", productCatalog);
        ProductCatalog productCatalog1 = productCatalogService.setupProductCatalog(productCatalog);
        log.info("Finished setting up Product Catalog: {}", productCatalog);
    }


}
