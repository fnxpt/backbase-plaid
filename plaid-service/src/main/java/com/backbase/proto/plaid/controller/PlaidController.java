package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.api.PlaidApi;
import com.backbase.proto.plaid.model.PlaidLinkRequest;
import com.backbase.proto.plaid.model.PlaidLinkResponse;
import com.backbase.proto.plaid.model.PublicTokenRequest;
import com.backbase.proto.plaid.service.PlaidLinkService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PlaidController implements PlaidApi {

    private final PlaidLinkService plaidLinkService;

    public PlaidController(PlaidLinkService plaidLinkService) {
        this.plaidLinkService = plaidLinkService;
        log.info("Plaid Controller created");
    }

    @Override
    public ResponseEntity<PlaidLinkResponse> requestPlaidLink(@Valid PlaidLinkRequest plaidLinkRequest) {
        log.info("Requesting Plaid Link: {}", plaidLinkRequest);
        PlaidLinkResponse plaidLink = plaidLinkService.createPlaidLink(plaidLinkRequest);
        return ResponseEntity.ok(plaidLink);
    }

    @Override
    public ResponseEntity<PlaidLinkResponse> setPublicAccessToken(@Valid PublicTokenRequest publicTokenRequest) {

        log.info("Set Plaid Public Token: {}", publicTokenRequest);
        plaidLinkService.setPublicAccessToken(publicTokenRequest);



        return null;
    }


}
