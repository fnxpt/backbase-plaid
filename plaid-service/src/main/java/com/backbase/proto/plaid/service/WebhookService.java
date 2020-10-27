package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.Webhook;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.repository.WebhookRepository;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemWebhookUpdateRequest;
import com.plaid.client.request.TransactionsRefreshRequest;
import com.plaid.client.response.ErrorResponse;
import com.plaid.client.response.TransactionsRefreshResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import static com.backbase.proto.plaid.model.Webhook.WebhookType.*;
import static com.backbase.proto.plaid.model.Webhook.WebhookCode.*;


/**
 * This class sets up and processes Plaid webhook for used in Backbase DBS.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookService {

    private final PlaidClient plaidClient;

    private final PlaidConfigurationProperties plaidConfigurationProperties;

    private final PlaidTransactionsService transactionsService;

    private final WebhookRepository webhookRepository;

    private final ItemRepository itemRepository;

    /**
     * Sets up the webhook with configurations.
     *
     * @param accessToken provides authenticator in Plaid
     * @param itemId identifies Item for which the webhook is notifying for
     */
    public void setupWebhook(String accessToken, String itemId) {

        String webhookUrl = plaidConfigurationProperties.getWebhookBaseUrl() + "/webhook/" + itemId;
        log.info("Setting up webhook for item: {} with url:
        try {
            plaidClient.service().itemWebhookUpdate(new ItemWebhookUpdateRequest(accessToken, webhookUrl)).execute().body();
            log.info("Webhook Setup");
        } catch (IOException e) {
            throw new BadRequestException("Unable to setup web hook: ", e);
        }
    }

    /**
     * Processes the webhook for updates in Transactions or the Item.
     *
     * @param webhook webhook being used
     */

    public void process(Webhook webhook) {
        log.info("Processing webhook: {} for item: {}", webhook.getWebhookType(), webhook.getItemId());
        webhook.setCreatedAt(LocalDateTime.now());
        webhookRepository.save(webhook);
        try {
            validateWebhook(webhook);

            if(webhook.getWebhookType() == TRANSACTIONS)
                processTransactions(webhook);
            else if(webhook.getWebhookType() == ITEM)
                processItem(webhook);

            webhook.setCompleted(true);
        } catch (Exception exception) {
            exception.printStackTrace();
            webhook.setError(exception.getMessage());
        }
        webhookRepository.save(webhook);
    }

    /**
     * Validates the webhook to see if it is really coming from Plaid, it throws an exception if it doesn't.
     *
     * @param webhook the webhook to be validated
     */
    private void validateWebhook(Webhook webhook) {
        //TODO:
        // need a jwt to validate
        // getInternalJwt in plaid link service
        // once have the header of the jwt use the webhook_verification_key/get endpoint to verify
        // https://plaid.com/docs/api/webhook-verification/
        // Validate if web hook is really coming from plaid. Throw exception if it doesn't
    }

    /**
     * Processes the webhook for updates in Transactions for an Item, depending on the webhooks code it will update a
     * transaction differently.
     *
     * @param webhook webhook to process for updates
     */
    private void processTransactions(Webhook webhook) {

        switch (webhook.getWebhookCode()) {
            case INITIAL_UPDATE: {
                // Fired when an Item's initial transaction pull is completed.
                // Note: The default pull is 30 days.
                log.info("Process Initial Update");
                transactionsService.ingestInitialUpdate(webhook.getItemId());
                break;
            }
            case HISTORICAL_UPDATE: {
                transactionsService.ingestHistoricalUpdate(webhook.getItemId());
                break;
            }
            case DEFAULT_UPDATE: {
                log.info("Process Default Update");
                transactionsService.ingestDefaultUpdate(webhook.getItemId());
                break;
            }
            case TRANSACTIONS_REMOVED: {
                log.info("Process transactions removed");
                transactionsService.removeTransactions( webhook.getRemovedTransactions());
                break;
            }
            default: {
                throw new BadRequestException("Not a valid webhook code");
            }
        }

    }

    /**
     * The webhook updates the Item database.
     *
     * @param webhook that notifies Item updates
     */
    private void processItem(Webhook webhook) {
        log.info("Webhook Acknowledged");
        //TODO: Update Item Database. Update Token Status HERE
        switch (webhook.getWebhookCode()){
            case ERROR: {
                log.info("Issue with Item, Resolved by going through link update");
                break;
            }
            case PENDING_EXPIRATION: {
                log.info("The Items access token will expire in 7 days, Resolved by going through link update");
                Optional<Item> byItemId = itemRepository.findByItemId(webhook.getItemId());
                byItemId.ifPresent(item -> {
                    item.setExpiryDate(LocalDate.now().plusDays(7));
                    itemRepository.save(item);

                } );
                break;
            }
            case USER_PERMISSION_REVOKED: {
                log.info("The end user has revoked the permission of access to an Item, Resolved by creating a new Item");
                break;
            }
            case WEBHOOK_UPDATE_ACKNOWLEDGED: {
                log.info("The Item's webhook is updated");
                break;
            }
            default:
                throw new BadRequestException("Not a valid webhook code");
        }
    }


    /**
     * Refreshes the Transactions for a specific Item. Updates the Transactions and processes them.
     *
     * @param itemId identifies the set of Transactions to be updated by which Item it belongs to
     */
    @SneakyThrows
    public void refresh(String itemId) {
        log.info("Refreshing Transactions for: {}", itemId);
        Item item = itemRepository.findByItemId(itemId).orElseThrow(() -> new BadRequestException("Invalid item id: " + itemId));
        TransactionsRefreshRequest transactionsRefreshRequest = new TransactionsRefreshRequest(item.getAccessToken());
        transactionsRefreshRequest.clientId = plaidConfigurationProperties.getClientId();
        transactionsRefreshRequest.secret = plaidConfigurationProperties.getSecret();
        Response<TransactionsRefreshResponse> execute = plaidClient.service().transactionsRefresh(transactionsRefreshRequest).execute();
        if(execute.isSuccessful()) {
            TransactionsRefreshResponse body = execute.body();
            assert body != null;
            log.info("Refresh response: {}", body.getRequestId());
        } else {
            ErrorResponse errorResponse = plaidClient.parseError(execute);
            log.error("Error refreshing transactions: {}", errorResponse.getErrorMessage());
        }


    }
}
