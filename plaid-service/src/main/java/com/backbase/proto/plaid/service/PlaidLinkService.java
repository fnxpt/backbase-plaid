package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtil;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwt;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.mapper.ItemMapper;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.PlaidLinkRequest;
import com.backbase.proto.plaid.model.PlaidLinkResponse;
import com.backbase.proto.plaid.model.SetAccessTokenRequest;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.stream.TransactionService;
import com.backbase.stream.configuration.AccessControlConfiguration;
import com.backbase.stream.product.ProductIngestionSagaConfiguration;
import com.backbase.stream.productcatalog.configuration.ProductCatalogServiceConfiguration;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.LinkTokenCreateRequest;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.LinkTokenCreateResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import retrofit2.Response;

/**
 * allows the retrieval and ingestion of Plaid link data when it is available from plaid
 */
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

    private final ItemRepository itemRepository;

    private final ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    private final AccountService accountService;

    private final WebhookService webhookService;

    private final PlaidTransactionsService transactionService;

    private final SecurityContextUtil securityContextUtil;

    private Executor tempExecutor = Executors.newSingleThreadExecutor();

    /**
     * creates a plaid link token
     * @param plaidLinkRequest contains some link fields to be used and comparators
     * @return link token
     */
    public PlaidLinkResponse createPlaidLink(@Valid PlaidLinkRequest plaidLinkRequest) {
        try {
            String redirectUrl = null;
            LinkTokenCreateRequest.User user = new LinkTokenCreateRequest.User(getLoggedInUserId());
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
            assert body != null;
            String linkToken = body.getLinkToken();
            log.info("link token: {}", linkToken);
            return new PlaidLinkResponse().token(linkToken);
        } catch (IOException e) {
            throw new BadRequestException(e);
        }
    }

    /**
     * retrieves an access token for a legal entity and sets up a webhook and triggers the ingestion of accounts
     * using it
     * @param setAccessTokenRequest request body for the access token request
     */
    public void setPublicAccessToken(@Valid SetAccessTokenRequest setAccessTokenRequest) {
        ItemPublicTokenExchangeResponse accessToken = requestAccessToken(setAccessTokenRequest);
        InternalJwt internalJwt = getInternalJwt();
        String userId = getLoggedInUserId(internalJwt);
        String legalEntityId = getLoggedInLegalEntityInternal(internalJwt);

        String itemId = accessToken.getItemId();
        if(!itemRepository.existsByItemId(itemId)) {
            log.info("Saving item: {}", itemId);
            createItem(accessToken, userId);
        } else {
            log.info("Item already exists: {}", itemId);
        }

        accountService.ingestPlaidAccounts(accessToken.getAccessToken(), userId, legalEntityId);
        webhookService.setupWebhook(accessToken.getAccessToken(), itemId);
        setupWebHook(accessToken);

        tempExecutor.execute(() -> {
            transactionService.ingestDefaultUpdate(itemId);
        });
    }

    /**
     * creates an item and saves it in the item database
     * @param accessToken authentication for item mapping
     * @param userId
     */
    @NotNull
    private void createItem(ItemPublicTokenExchangeResponse accessToken, String userId) {
        Item newItem = itemMapper.map(accessToken);
        newItem.setCreatedAt(LocalDateTime.now());
        newItem.setCreatedBy(userId);
        itemRepository.save(newItem);
    }

    /**
     * sets up a webhook
     * @param accessToken used for authentication in plaid
     */
    private void setupWebHook(ItemPublicTokenExchangeResponse accessToken) {

    }

    /**
     * exchanges public token for access token
     * @param setAccessTokenRequest request body for the exchange
     * @return access token
     */
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

    /**
     * gets the users id from internal token
     * @return users id
     */
    private String getLoggedInUserId() {
        return getLoggedInUserId(getInternalJwt());
    }

    /**
     * Gets the logged in users User Id from the internal token used to perform action on their behalf
     * @param internalJwt token useed for internal authentication
     * @return subject of the internal token
     */
    private String getLoggedInUserId(InternalJwt internalJwt) {
        return internalJwt.getClaimsSet().getSubject().orElseThrow(() -> new IllegalStateException("Cannot get subject"));
    }

    /**
     * gets logged in legal entity id from the internal token
     * @param internalJwt token used for internal authentication
     * @return legal entity id or exception
     */
    private String getLoggedInLegalEntityInternal(InternalJwt internalJwt) {
        Optional<Object> leid = internalJwt.getClaimsSet().getClaim("leid");
        return leid.orElseThrow(() -> new IllegalStateException("Cannot get subject")).toString();
    }

    /**
     * retrieve internal token
     * @return internal token
     */
    private InternalJwt getInternalJwt() {
        return securityContextUtil.getOriginatingUserJwt().orElseThrow(() -> new IllegalStateException("Cannnot get internal JWT"));
    }
}
