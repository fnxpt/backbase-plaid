package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtil;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwt;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.exceptions.IngestionFailedException;
import com.backbase.proto.plaid.mapper.ItemMapper;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.PlaidLinkRequest;
import com.backbase.proto.plaid.model.PlaidLinkResponse;
import com.backbase.proto.plaid.model.SetAccessTokenRequest;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.stream.configuration.AccessControlConfiguration;
import com.backbase.stream.product.ProductIngestionSagaConfiguration;
import com.backbase.stream.productcatalog.configuration.ProductCatalogServiceConfiguration;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.LinkTokenCreateRequest;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.LinkTokenCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * This class allows the retrieval and ingestion of Plaid link data when it is available from Plaid.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Import({
    ProductIngestionSagaConfiguration.class,
    AccessControlConfiguration.class,
    ProductCatalogServiceConfiguration.class,

})
public class LinkService {

    private final PlaidClient plaidClient;
    private final PlaidConfigurationProperties plaidConfigurationProperties;

    private final ItemRepository itemRepository;

    private final ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    private final AccountService accountService;

    private final WebhookService webhookService;

    private final ItemService itemService;

    private final TransactionsService transactionService;

    private final SecurityContextUtil securityContextUtil;

    private final Executor tempExecutor = Executors.newSingleThreadExecutor();

    /**
     * Creates a Plaid Link Token
     *
     * @param plaidLinkRequest contains some link fields to be used and comparators
     * @return Link Token
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
     * Retrieves an Access Token for a Legal Entity and sets up a webhook and triggers the ingestion of accounts
     * using it.
     *
     * @param setAccessTokenRequest request body for the Access Token request
     */
    public void setPublicAccessToken(@Valid SetAccessTokenRequest setAccessTokenRequest) {
        ItemPublicTokenExchangeResponse accessToken = requestAccessToken(setAccessTokenRequest);
        InternalJwt internalJwt = getInternalJwt();
        String userId = getLoggedInUserId(internalJwt);
        String legalEntityId = getLoggedInLegalEntityInternal(internalJwt);


        Item item = itemRepository.findByItemId(accessToken.getItemId())
            .orElseGet(() -> createItem(accessToken, userId));

        accountService.ingestPlaidAccounts(accessToken.getAccessToken(), userId, legalEntityId);
        webhookService.setupWebhook(accessToken.getAccessToken(), item);
        setupWebHook(accessToken);

        tempExecutor.execute(() -> {
            try {
                transactionService.ingestDefaultUpdate(item);
            } catch (IngestionFailedException e) {
                log.error("Failed to ingest transactions for item: " + item, e);
            }
        });
    }

    /**
     * Creates an Item and saves it in the Item database.
     *
     * @param accessToken authentication for Item mapping
     * @param userId
     */
    @NotNull
    private Item createItem(ItemPublicTokenExchangeResponse accessToken, String userId) {
        Item newItem = itemMapper.map(accessToken);
        newItem.setCreatedAt(LocalDateTime.now());
        newItem.setCreatedBy(userId);
        itemRepository.save(newItem);
        return newItem;
    }

    /**
     * Sets up a webhook.
     *
     * @param accessToken used for authentication in Plaid
     */
    private void setupWebHook(ItemPublicTokenExchangeResponse accessToken) {
      // does nothing because it is yet to be implemented ?
        // should get rid of unused perimeter
    }

    /**
     * Exchanges Public Token for Access Token.
     *
     * @param setAccessTokenRequest request body for the exchange
     * @return Access Token
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
     * Gets the users ID from Internal Token.
     *
     * @return users ID
     */
    private String getLoggedInUserId() {
        return getLoggedInUserId(getInternalJwt());
    }

    /**
     * Gets the logged in user's User Id from the Internal Token used to perform action on their behalf.
     *
     * @param internalJwt token used for internal authentication
     * @return subject of the Internal Token
     */
    private String getLoggedInUserId(InternalJwt internalJwt) {
        return internalJwt.getClaimsSet().getSubject().orElseThrow(() -> new IllegalStateException("Cannot get subject"));
    }

    /**
     * Gets logged in Legal Entity ID from the Internal Token.
     *
     * @param internalJwt token used for internal authentication
     * @return Legal Entity ID or exception
     */
    private String getLoggedInLegalEntityInternal(InternalJwt internalJwt) {
        Optional<Object> leid = internalJwt.getClaimsSet().getClaim("leid");
        return leid.orElseThrow(() -> new IllegalStateException("Cannot get subject")).toString();
    }

    /**
     * Retrieve Internal Token.
     *
     * @return Internal Token
     */
    private InternalJwt getInternalJwt() {
        return securityContextUtil.getOriginatingUserJwt().orElseThrow(() -> new IllegalStateException("Cannnot get internal JWT"));
    }


}
