package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtil;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwt;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.model.PlaidLinkRequest;
import com.backbase.proto.plaid.model.PlaidLinkResponse;
import com.backbase.proto.plaid.model.SetAccessTokenRequest;
import com.backbase.stream.configuration.AccessControlConfiguration;
import com.backbase.stream.legalentity.model.AvailableBalance;
import com.backbase.stream.legalentity.model.BookedBalance;
import com.backbase.stream.legalentity.model.CreditLimit;
import com.backbase.stream.legalentity.model.JobProfileUser;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.legalentity.model.LegalEntityReference;
import com.backbase.stream.legalentity.model.Product;
import com.backbase.stream.legalentity.model.ProductGroup;
import com.backbase.stream.legalentity.model.ServiceAgreement;
import com.backbase.stream.legalentity.model.User;
import com.backbase.stream.product.ProductIngestionSaga;
import com.backbase.stream.product.ProductIngestionSagaConfiguration;
import com.backbase.stream.product.task.ProductGroupTask;
import com.backbase.stream.productcatalog.ProductCatalogService;
import com.backbase.stream.productcatalog.configuration.ProductCatalogServiceConfiguration;
import com.backbase.stream.productcatalog.model.ProductCatalog;
import com.backbase.stream.productcatalog.model.ProductKind;
import com.backbase.stream.productcatalog.model.ProductType;
import com.backbase.stream.service.AccessGroupService;
import com.backbase.stream.service.LegalEntityService;
import com.backbase.stream.worker.exception.StreamTaskException;
import com.backbase.stream.worker.model.StreamTask;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.AccountsBalanceGetRequest;
import com.plaid.client.request.InstitutionsGetByIdRequest;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.LinkTokenCreateRequest;
import com.plaid.client.response.Account;
import com.plaid.client.response.AccountsBalanceGetResponse;
import com.plaid.client.response.Institution;
import com.plaid.client.response.InstitutionsGetByIdResponse;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.ItemStatus;
import com.plaid.client.response.LinkTokenCreateResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import static org.mockito.Mockito.*;
@Service
@Slf4j
@RequiredArgsConstructor
@Import({
    ProductIngestionSagaConfiguration.class,
    AccessControlConfiguration.class,
    ProductCatalogServiceConfiguration.class,

})
public class PlaidLinkService {

    private final PlaidClient plaidClient;
    private final PlaidConfigurationProperties plaidConfigurationProperties;
    private final SecurityContextUtil securityContextUtil;

    private final ProductIngestionSaga productIngestionSaga;
    private final ProductCatalogService productCatalogService;

    private final AccessGroupService accessGroupService;

    private final LegalEntityService legalEntityService;

    public PlaidLinkResponse createPlaidLink(@Valid PlaidLinkRequest plaidLinkRequest) {

        InternalJwt internalJwt = getInternalJwt();
        String userId = getLoggedInUserId(internalJwt);

        String redirectUrl = null;

        try {
            LinkTokenCreateRequest.User user = new LinkTokenCreateRequest.User(userId);

            Response<LinkTokenCreateResponse> response =
                this.plaidClient.service().linkTokenCreate(new LinkTokenCreateRequest(
                    user,
                    plaidConfigurationProperties.getClientName(),
                    plaidConfigurationProperties.getProducts().stream()
                        .map(PlaidConfigurationProperties.Product::toString)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList()),
                    plaidConfigurationProperties.getCountryCodes().stream()
                        .map(PlaidConfigurationProperties.CountryCode::toString)
                        .collect(Collectors.toList()),
                    plaidLinkRequest.getLanguage())
                    .withRedirectUri(redirectUrl))
                    .execute();
            LinkTokenCreateResponse body = response.body();
            log.info("plaid link response{}",body);

            assert body != null;
            String linkToken = body.getLinkToken();
            log.info("link token {}", linkToken);
            return new PlaidLinkResponse().token(linkToken);
        } catch (IOException e) {
            throw new BadRequestException(e);
        }
    }

    private String getLoggedInUserId(InternalJwt internalJwt) {
        return internalJwt.getClaimsSet().getSubject().orElseThrow(() -> new IllegalStateException("Cannot get subject"));
    }

    private String getLoggedInLegalEntityInternal(InternalJwt internalJwt) {
        Optional<Object> leid = internalJwt.getClaimsSet().getClaim("leid");
        return leid.orElseThrow(() -> new IllegalStateException("Cannot get subject")).toString();
    }

    private InternalJwt getInternalJwt() {
        return securityContextUtil.getOriginatingUserJwt().orElseThrow(() -> new IllegalStateException("Cannnot get internal JWT"));
    }

    public void setPublicAccessToken(@Valid SetAccessTokenRequest setAccessTokenRequest) {
        ItemPublicTokenExchangeResponse accessToken = requestAccessToken(setAccessTokenRequest);
        InternalJwt internalJwt = getInternalJwt();
        String userId = getLoggedInUserId(internalJwt);
        String legalEntityId = getLoggedInLegalEntityInternal(internalJwt);
        ingestPlaidAccounts(accessToken.getAccessToken(), userId, legalEntityId);
    }

    public void ingestPlaidAccounts(String accessToken, String userId, String legalEntityId) {

        AccountsBalanceGetResponse plaidAccounts = requestPlaidAccounts(accessToken);
        List<Account> accounts = plaidAccounts.getAccounts();

        createProductCatalogFrom(accounts);

        ItemStatus item = plaidAccounts.getItem();
        String institutionId = item.getInstitutionId();
        Institution institution = getInstitution(institutionId);


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
            .map(account -> mapAccount(accessToken, item, institution, account)).collect(Collectors.toList()));

        productIngestionSaga.process(new ProductGroupTask().data(productGroup))
            .doOnNext(StreamTask::logSummary)
            .doOnError(StreamTaskException.class, e -> {
                log.error("Failed ot setup Product Group: {}", e.getMessage(), e);
                e.getTask().logSummary();
            })
            .block();

    }

    private Product mapAccount(String accessToken, ItemStatus item, Institution institution, Account account) {
        Map<String, Object> additions = new HashMap<>();
        additions.put("plaidAccessToken", accessToken);
        additions.put("plaidItemId", item.getItemId());
        additions.put("plaidInstitutionId", institution.getInstitutionId());
        additions.put("plaidAccountOfficialName", account.getOfficialName());
        additions.put("institutionName", institution.getName());
        additions.put("institutionLogo", institution.getLogo());

        Product product = new Product();
        product.setExternalId(account.getAccountId());
        product.setName(account.getName());
//        product.setBankAlias(account.getOfficialName());
        product.setAdditions(additions);
        String productTypeExternalId = mapSubTypeId(account.getSubtype());
        product.setProductTypeExternalId(productTypeExternalId);
        product.setBBAN(account.getMask());

        Account.Balances balances = account.getBalances();
        if (balances != null) {
            product.setCurrency(balances.getIsoCurrencyCode());
            if (Objects.nonNull(balances.getAvailable())) {
                product.setAvailableBalance(new AvailableBalance()
                    .amount(BigDecimal.valueOf(balances.getAvailable()))
                    .currencyCode(balances.getIsoCurrencyCode()));
            }
            if (Objects.nonNull(balances.getCurrent())) {
                product.setBookedBalance(new BookedBalance()
                    .amount(BigDecimal.valueOf(balances.getCurrent()))
                    .currencyCode(balances.getIsoCurrencyCode()));
            }
            if (Objects.nonNull(balances.getLimit())) {
                product.setCreditLimit(new CreditLimit()
                    .amount(BigDecimal.valueOf(balances.getLimit()))
                    .currencyCode(balances.getIsoCurrencyCode()));
            }
        }
        return product;
    }

    private Institution getInstitution(String institutionId) {
        InstitutionsGetByIdResponse institutionsGetResponse = null;
        try {
            institutionsGetResponse = plaidClient.service()
                .institutionsGetById(new InstitutionsGetByIdRequest(institutionId))
                .execute()
                .body();
        } catch (IOException e) {
            throw new BadRequestException("Failed to get institution by Id: " + institutionId);
        }
        return institutionsGetResponse.getInstitution();
    }

    public void createProductCatalogFrom(List<Account> plaidAccounts) {
        ProductCatalog productCatalog = new ProductCatalog();
        productCatalog.setProductTypes(new ArrayList<>());
        plaidAccounts.stream()
            .collect(Collectors.groupingBy(Account::getType))
            .forEach((type, accounts) -> {
                String kindId = mapProductType(type, "external-");
                ProductKind productKindsItem = new ProductKind()
                    .externalKindId(kindId)
                    .kindUri(kindId)
                    .kindName("External " + StringUtils.capitalize(type));
                productCatalog.addProductKindsItem(productKindsItem);
                productCatalog.getProductTypes().addAll((accounts.stream()
                    .map(Account::getSubtype).collect(Collectors.toSet())
                    .stream().map(subtype -> {
                        String productTypeId = mapSubTypeId(subtype);
                        return new ProductType()
                            .externalId(productTypeId)
                            .externalProductId(productTypeId)
                            .externalProductKindId(kindId)
                            .productTypeName(StringUtils.capitalize(subtype));
                    })
                    .collect(Collectors.toList())));
            });
        log.info("Setting up Product Catalog");
        productCatalogService.setupProductCatalog(productCatalog);
    }

    private String mapSubTypeId(String subtype) {
        return "external-" + subtype.replace(" ", "-");
    }

    private String mapProductType(String type, String s) {
        return s + type;
    }

    public AccountsBalanceGetResponse requestPlaidAccounts(String accessToken) {
        try {
            return plaidClient.service().accountsBalanceGet(new AccountsBalanceGetRequest(accessToken)).execute().body();
        } catch (IOException e) {
            throw new BadRequestException("Failed to get access token");
        }
    }


    private ItemPublicTokenExchangeResponse requestAccessToken(SetAccessTokenRequest setAccessTokenRequest) {
        try {
            ItemPublicTokenExchangeResponse accessToken = plaidClient
                .service()
                .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(setAccessTokenRequest.getPublicToken()))
                .execute()
                .body();
            log.info("Access Token: {}", accessToken.getAccessToken());
            log.info("Item ID: {}", accessToken.getItemId());
            return accessToken;
        } catch (IOException e) {
            throw new BadRequestException("Invalid Plaid Public Token: " + setAccessTokenRequest.getPublicToken());
        }
    }
}
