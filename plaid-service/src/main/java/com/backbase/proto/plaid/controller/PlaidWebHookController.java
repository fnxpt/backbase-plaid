package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.api.WebhookApi;
import com.backbase.proto.plaid.mapper.WebhookMapper;
import com.backbase.proto.plaid.model.InlineObject;
import com.backbase.proto.plaid.model.PlaidWebhook;
import com.backbase.proto.plaid.model.Webhook;
import com.backbase.proto.plaid.service.WebhookService;
import java.util.Map;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * PlaidWebHookController:
 * Sets up and builds a plaid webhook, this webhook notifies dbs when data is available for retrieval
 */
@Slf4j
@RestController
@RequiredArgsConstructor

public class PlaidWebHookController implements WebhookApi {

    private final WebhookService webhookService;

    private final WebhookMapper webhookMapper = Mappers.getMapper(WebhookMapper.class);

    /**
     * Initialises the webhook
     *
     * @param itemId the item who's accounts the data is being retrieved
     * @param plaidWebhook webhook that will be notifying available data in the item passed
     * @return Http response that indicated successfully retrieving a webhook
     */
    @Override
    public ResponseEntity<Void> processWebHook(String itemId, @Valid PlaidWebhook plaidWebhook) {
        log.info("Received Plaid Webhook!");
        webhookService.process(webhookMapper.mapToDomain(plaidWebhook));
        return ResponseEntity.accepted().build();
    }

//    @Override
//    public ResponseEntity<Void> processWebHook(Map<String, Object> requestBody) {
//        log.info("Received Plaid Webhook!");
//        Webhook webhook = new Webhook();
//        webhook.setItemId((String)requestBody.get("item_id"));
//        webhook.setWebhookCode(Webhook.WebhookCode.valueOf(requestBody.get("webhook_code").toString()));
//        webhook.setWebhookType(Webhook.WebhookType.valueOf(requestBody.get("webhook_type").toString()));
//        webhook.setError((String)requestBody.get("error"));
//
//        webhookService.process(webhook);
////        webhookService.process(plaidWebhook);
//        return ResponseEntity.accepted().build();
//    }

    /**
     * Refreshes, updates the transactions for an item identified by its item id
     *
     * @param itemId The plaid item id to refresh (required)
     * @param inlineObject  (optional)
     * @return http response indicating the success of the operation
     */
    @Override
    public ResponseEntity<Void> refreshTransactions(String itemId, @Valid InlineObject inlineObject) {
        log.info("refreshingItem!");
        webhookService.refresh(itemId);
        return ResponseEntity.accepted().build();
    }
}
