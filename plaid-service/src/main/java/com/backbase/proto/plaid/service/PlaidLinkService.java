package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtil;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwt;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.model.PlaidLinkRequest;
import com.backbase.proto.plaid.model.PlaidLinkResponse;
import com.backbase.proto.plaid.model.PublicTokenRequest;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.LinkTokenCreateRequest;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.LinkTokenCreateResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Response;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaidLinkService {

    private final PlaidClient plaidClient;
    private final PlaidConfigurationProperties plaidConfigurationProperties;
    private final SecurityContextUtil securityContextUtil;


    public PlaidLinkResponse createPlaidLink(@Valid PlaidLinkRequest plaidLinkRequest) {

        InternalJwt internalJwt = securityContextUtil.getOriginatingUserJwt().orElseThrow(() -> new IllegalStateException("Cannnot get internal JWT"));
        String subject = internalJwt.getClaimsSet().getSubject().orElseThrow(() -> new IllegalStateException("Cannot get subject"));

        String redirectUrl = null;

        try {
            LinkTokenCreateRequest.User user = new LinkTokenCreateRequest.User(subject);

            Response<LinkTokenCreateResponse> response =
                this.plaidClient.service().linkTokenCreate(new LinkTokenCreateRequest(
                    user,
                    "Plaid Quickstart",
                    Arrays.stream(plaidConfigurationProperties.getProducts()).map(PlaidConfigurationProperties.Product::toString).map(String::toLowerCase).collect(Collectors.toList()),
                    Arrays.stream(plaidConfigurationProperties.getCountryCodes()).map(PlaidConfigurationProperties.CountryCode::toString).collect(Collectors.toList()),
                    plaidLinkRequest.getLanguage()
                ).withRedirectUri(redirectUrl))
                    .execute();
            LinkTokenCreateResponse body = response.body();

            return new PlaidLinkResponse().token(body.getLinkToken());
        } catch (IOException e) {
            throw new BadRequestException(e);
        }
    }

    public void setPublicAccessToken(PublicTokenRequest publicTokenRequest) {

        try {
            ItemPublicTokenExchangeResponse accessToken = plaidClient.service().itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicTokenRequest.getPublicToken()))
                .execute().body();

            log.info("Access Token: {}", accessToken.getAccessToken());
            log.info("Item ID: {}", accessToken.getItemId());

        } catch (IOException e) {
            throw new BadRequestException(e);
        }
    }
}
