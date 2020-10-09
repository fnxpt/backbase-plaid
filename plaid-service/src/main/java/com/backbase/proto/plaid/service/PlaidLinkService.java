package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtil;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.model.PlaidLinkRequest;
import com.backbase.proto.plaid.model.PlaidLinkResponse;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.LinkTokenCreateRequest;
import com.plaid.client.response.LinkTokenCreateResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
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

        Principal principal = SecurityContextHolder.getContext().getAuthentication();


        String redirectUrl = "http://localhost:8080";

        try {
            LinkTokenCreateRequest.User user = new LinkTokenCreateRequest.User("user-id");
            Response<LinkTokenCreateResponse> response =
                this.plaidClient.service().linkTokenCreate(new LinkTokenCreateRequest(
                    user,
                    "Plaid Quickstart",
                    Arrays.stream(plaidConfigurationProperties.getProducts()).map(PlaidConfigurationProperties.Product::toString).map(String::toLowerCase).collect(Collectors.toList()),
                    Arrays.stream(plaidConfigurationProperties.getCountryCodes()).map(String::toLowerCase).collect(Collectors.toList()),
                    plaidLinkRequest.getLanguage()
                ).withRedirectUri(redirectUrl))
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new PlaidLinkResponse();
    }


}
