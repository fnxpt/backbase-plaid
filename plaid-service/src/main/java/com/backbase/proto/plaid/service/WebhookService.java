package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.model.PlaidWebhook;
import com.backbase.proto.plaid.repository.WebhookRepository;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemWebhookUpdateRequest;
import com.plaid.client.request.WebhookVerificationKeyGetRequest;
import com.plaid.client.response.ItemWebhookUpdateResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookService {

    private final PlaidClient plaidClient;

    private final PlaidConfigurationProperties plaidConfigurationProperties;

    private final PlaidTransactionsService transactionsService;

    private final WebhookRepository webhookRepository;

    public void setupWebhook(String accessToken, String itemId) {

        String webhookUrl = plaidConfigurationProperties.getWebhookBaseUrl() + "/webhook/" + itemId;

        log.info("Setting up webhook for item: {} with url:
        try {
            ItemWebhookUpdateResponse body = plaidClient.service().itemWebhookUpdate(new ItemWebhookUpdateRequest(accessToken, webhookUrl)).execute().body();
            log.info("Webhook Setup");
        } catch (IOException e) {
            throw new BadRequestException("Unable to setup web hook: ", e);
        }
    }

    public void process(PlaidWebhook plaidWebhook) {

        validateWebhook(plaidWebhook);

        switch (plaidWebhook.getWebhookType()) {
            case TRANSACTIONS:
                processTransactions(plaidWebhook);
                break;
            case ITEM:
                processItem(plaidWebhook);
        }

    }

    private void validateWebhook(PlaidWebhook plaidWebhook) {

//        plaidClient.service().getWebhookVerificationKey(new WebhookVerificationKeyGetRequest(plaidWebhook.get))

    }

    private void processTransactions(PlaidWebhook plaidWebhook) {

        switch (plaidWebhook.getWebhookCode()) {
            case INITIAL_UPDATE: {
                // Fired when an Item's initial transaction pull is completed.
                // Note: The default pull is 30 days.
                log.info("Process Initial Update");
//                transactionsService.ingestTransactions();

                break;
            }
            case HISTORICAL_UPDATE: {
                log.info("Process Historical Update");
                break;
            }
            case DEFAULT_UPDATE: {
                log.info("Process Default Update");
                break;
            }
            case TRANSACTIONS_REMOVED: {
                log.info("Process transactions removed");
            }
            default: {
                throw new BadRequestException("Not a valid web hook code");
            }
        }

    }


    private void processItem(PlaidWebhook plaidWebhook) {

    }


}
