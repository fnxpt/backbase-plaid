package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.mapper.AccountMapper;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.proto.plaid.model.Item;
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
import com.plaid.client.response.ErrorResponse;
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
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Response;

import static com.backbase.proto.plaid.utils.ProductTypeUtils.mapProductType;
import static com.backbase.proto.plaid.utils.ProductTypeUtils.mapSubTypeId;

/**
 * This class allows the retrieval and ingestion of account data when it is available from Plaid.
 */
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

    private final TransactionsService transactionService;

    private final ProductCatalogService productCatalogService;

    /**
     * Sends a request to Plaid for the accounts of a given item by parsing in the Access Token.
     *
     * @param accessToken authenticates the request of actions on Item data
     * @return the account account balance of the account requested for
     */
    public AccountsBalanceGetResponse requestPlaidAccounts(String accessToken) {
        try {
            Response<AccountsBalanceGetResponse> execute = plaidClient.service().accountsBalanceGet(new AccountsBalanceGetRequest(accessToken)).execute();
            if (execute.isSuccessful()) {
                return execute.body();
            } else {
                ErrorResponse errorResponse = plaidClient.parseError(execute);
                throw new BadRequestException("Cannot get accounts from Plaid: " + errorResponse.getErrorMessage());
            }
        } catch (IOException e) {
            throw new BadRequestException("Failed to get access token");
        }
    }

    /**
     * Ingests the accounts retrieved from Plaid into Backbase, it maps and stores the accounts in the account database
     * it sets additional data required for ingestion such Service Agreements.
     *
     * @param accessToken   used to retrieve account data from Plaid
     * @param userId        required for institution retrieval
     * @param legalEntityId used to get Service and Master Agreements which are needed to perform action on the accounts ingested
     */
    public void ingestPlaidAccounts(Item item, String accessToken, String userId, String legalEntityId) {
        AccountsBalanceGetResponse plaidAccounts = requestPlaidAccounts(accessToken);

        List<Account> accounts = plaidAccounts.getAccounts();


        accounts.forEach(account -> {
            if (!accountRepository.existsByAccountId(account.getAccountId())) {
                log.info("Saving account: {}", account.getName());
                accountRepository.save(accountMapper.mapToDomain(account, plaidAccounts.getItem().getItemId()));
            } else {
                log.info("Account: {} already exists", account.getName());
            }
        });

        ItemStatus itemStatus = plaidAccounts.getItem();
        String institutionId = itemStatus.getInstitutionId();
        Institution institution = institutionService.getInstitution(institutionId, userId);
        item.setInstitutionId(institutionId);

        setupProductCatalog(accounts, institution);

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
     * Ensure that all types and sub types from Plaid exists in Backbase DBS Product Catalog.
     *
     * @param plaidAccounts List of linked Plaid Accounts
     */
    public void setupProductCatalog(List<Account> plaidAccounts, Institution institution) {
        ProductCatalog productCatalog = new ProductCatalog();
        productCatalog.setProductTypes(new ArrayList<>());
        plaidAccounts.stream()
            .collect(Collectors.groupingBy(Account::getType))
            .forEach((type, accounts) -> {
                String kindId = mapProductType(institution, type);
                ProductKind productKindsItem = new ProductKind()
                    .externalKindId(kindId)
                    .kindUri(kindId)
                    .kindName(institution.getName() + " " + StringUtils.capitalize(type));
                productCatalog.addProductKindsItem(productKindsItem);
                productCatalog.getProductTypes().addAll((accounts.stream()
                    .map(Account::getSubtype).collect(Collectors.toSet())
                    .stream().map(subtype -> {
                        String productTypeId = mapSubTypeId(institution, subtype);
                        return new ProductType()
                            .externalId(productTypeId)
                            .externalProductId(productTypeId)
                            .externalProductKindId(kindId)
                            .productTypeName(StringUtils.capitalize(subtype));
                    })
                    .collect(Collectors.toList())));
            });
        productCatalogService.setupProductCatalog(productCatalog);
    }


    @Transactional
    public void deleteAccountByItemId(Item item) {
        log.info("Deleteing account and it's transactions from Pliad");
        accountRepository.findAllByItemId(item.getItemId()).stream()
            .map(com.backbase.proto.plaid.model.Account::getAccountId)
            .forEach(accountId -> transactionService.deleteTransactionsByAccountId(item, accountId));
        accountRepository.deleteAccountsByItemId(item.getItemId());
    }


    public List<com.backbase.proto.plaid.model.Account> findAllByItemId(String itemId) {
        return accountRepository.findAllByItemId(itemId);
    }
}
