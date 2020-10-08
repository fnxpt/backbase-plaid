package com.backbase.proto.plaid.service;

import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.model.PlaidLinkRequest;
import com.backbase.proto.plaid.model.PlaidLinkResponse;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.LinkTokenCreateRequest;
import com.plaid.client.response.LinkTokenCreateResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Response;

@Service
@Slf4j
public class PlaidLinkService {

    private final PlaidClient plaidClient;
    private final List<String> countryCodes;
    private final List<String> plaidProducts;

    public PlaidLinkService(PlaidClient plaidClient, PlaidConfigurationProperties configurationProperties) {
        this.plaidClient = plaidClient;
        plaidProducts = Arrays.asList(configurationProperties.getPlaidProducts().split(","));
        countryCodes = Arrays.asList(configurationProperties.getPlaidCountryCodes().split(","));

    }

    public PlaidLinkResponse createPlaidLink(@Valid PlaidLinkRequest plaidLinkRequest) {

        String redirectUrl = "http://localhost:8080";

        try {
            Response<LinkTokenCreateResponse> response =
                this.plaidClient.service().linkTokenCreate(new LinkTokenCreateRequest(
                    new LinkTokenCreateRequest.User("user-id"),
                    "Plaid Quickstart",
                    this.plaidProducts,
                    this.countryCodes,
                    "en"
                ).withRedirectUri(redirectUrl))
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new PlaidLinkResponse();
    }


}
