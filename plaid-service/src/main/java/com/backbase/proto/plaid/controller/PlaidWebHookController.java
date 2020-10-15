package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.api.PlaidApi;
import com.backbase.proto.plaid.model.PlaidGetTransactionsResponse;
import com.backbase.proto.plaid.model.PlaidLinkRequest;
import com.backbase.proto.plaid.model.PlaidLinkResponse;
import com.backbase.proto.plaid.model.SetAccessTokenRequest;
import com.backbase.proto.plaid.service.PlaidLinkService;
import com.plaid.client.response.WebhookVerificationKeyGetResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.time.LocalDate;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PlaidWebHookController {

    private final PlaidLinkService plaidLinkService;

    public PlaidWebHookController(PlaidLinkService plaidLinkService) {
        this.plaidLinkService = plaidLinkService;
        log.info("Plaid Controller created");
    }


    @RequestMapping(value = "/webhook",
        produces = { "application/json" },
        method = RequestMethod.POST)
    public void handleWebHook(WebhookVerificationKeyGetResponse response) {

    }



}
