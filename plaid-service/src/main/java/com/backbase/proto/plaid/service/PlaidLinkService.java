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
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import retrofit2.Response;

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

    private final SecurityContextUtil securityContextUtil;

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

    public void setPublicAccessToken(@Valid SetAccessTokenRequest setAccessTokenRequest) {
        ItemPublicTokenExchangeResponse accessToken = requestAccessToken(setAccessTokenRequest);
        InternalJwt internalJwt = getInternalJwt();
        String userId = getLoggedInUserId(internalJwt);
        String legalEntityId = getLoggedInLegalEntityInternal(internalJwt);

        itemRepository.findByItemId(accessToken.getItemId())
            .orElseGet(() -> createItem(accessToken, userId));

        accountService.ingestPlaidAccounts(accessToken.getAccessToken(), userId, legalEntityId);
        webhookService.setupWebhook(accessToken.getAccessToken(), accessToken.getItemId());

        setupWebHook(accessToken);
    }

    @NotNull
    private Item createItem(ItemPublicTokenExchangeResponse accessToken, String userId) {
        Item newItem = itemMapper.map(accessToken);
        newItem.setCreatedAt(LocalDateTime.now());
        newItem.setCreatedBy(userId);
        return itemRepository.save(newItem);
    }

    private void setupWebHook(ItemPublicTokenExchangeResponse accessToken) {

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


    private String getLoggedInUserId() {
        return getLoggedInUserId(getInternalJwt());
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
}
