package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.api.WebhookApi;
import com.backbase.proto.plaid.model.PlaidWebhook;
import com.backbase.proto.plaid.service.WebhookService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PlaidWebHookController implements WebhookApi {

    private final WebhookService webhookService;

    @Override
    public ResponseEntity<Void> processWebHook(String itemId, @Valid PlaidWebhook plaidWebhook) {
        log.info("Received Plaid Webhook!");
        webhookService.process(plaidWebhook);
        return ResponseEntity.accepted().build();
    }
}
