package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.exceptions.IngestionFailedException;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.Webhook;
import com.backbase.proto.plaid.repository.WebhookRepository;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemWebhookUpdateRequest;
import com.plaid.client.request.TransactionsRefreshRequest;
import com.plaid.client.response.ErrorResponse;
import com.plaid.client.response.TransactionsRefreshResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Response;


/**
 * This class sets up and processes Plaid webhook for used in Backbase DBS.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookService {

    private final PlaidClient plaidClient;

    private final PlaidConfigurationProperties plaidConfigurationProperties;

    private final TransactionsService transactionsService;

    private final WebhookRepository webhookRepository;

    private final ItemService itemService;

    private final AccessTokenService accessTokenService;

    /**
     * Sets up the webhook with configurations.
     *
     * @param accessToken provides authenticator in Plaid
     * @param item        identifies Item for which the webhook is notifying for
     */
    public void setupWebhook(String accessToken, Item item) {

        String webhookUrl = plaidConfigurationProperties.getWebhookBaseUrl() + "/webhook/" + item.getItemId();
        log.info("Setting up webhook for item: {} with url:", item.getItemId(), webhookUrl);
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
        if (validateWebhook(webhook)) {
            switch (webhook.getWebhookType()) {
                case TRANSACTIONS:
                    processTransactionsCase(webhook);
                    break;
                case ITEM:
                    processItem(webhook);
                    break;
            }
            webhook.setCompleted(true);

            webhookRepository.save(webhook);
        }
    }

    private void processTransactionsCase(Webhook webhook){
        try {
            processTransactions(webhook);
        } catch (IngestionFailedException e) {
            log.error("Failed to ingest transactions for item: " + webhook.getItemId(), e);
            webhook.setError(e.getErrorResponse().getErrorCode());
        } catch (Exception e) {
            log.error("Failed to ingest transactions for item: " + webhook.getItemId(), e);
            webhook.setError(e.getMessage());
        }
    }

    /**
     * Validates the webhook to see if it is really coming from Plaid, it throws an exception if it doesn't.
     *
     * @param webhook the webhook to be validated
     */
    protected boolean validateWebhook(Webhook webhook) {


        // If we recieve a webhook from an deleted item, delete the webhook

        if (!itemService.isValidItem(webhook.getItemId())) {
            log.error("Webhook for item: {} is invalid, as item is not registered in Plaid database", webhook.getItemId());
            webhook.setCompleted(true);
            webhook.setError("WEBHOOK REFERS TO NON EXISTING ITEM");
            return false;
        }
        return true;

        //TODO:
        // Extract JWT from incoming header in the Plaid Webhook Controller
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
    private void processTransactions(Webhook webhook) throws IngestionFailedException {

        Item item = itemService.getValidItem(webhook.getItemId());

        switch (webhook.getWebhookCode()) {
            case INITIAL_UPDATE:
                transactionsService.ingestInitialUpdate(item);
                log.info("Process Initial Update");
                break;

            case HISTORICAL_UPDATE:
                transactionsService.ingestHistoricalUpdate(item);
                break;

            case DEFAULT_UPDATE:
                log.info("Process Default Update");
                transactionsService.ingestDefaultUpdate(item);
                break;

            case TRANSACTIONS_REMOVED:
                log.info("Process transactions removed");
                transactionsService.removeTransactions( webhook.getRemovedTransactions());
                break;

            default:
                throw new BadRequestException("Not a valid webhook code");

        }

    }

    /**
     * The webhook updates the Item database.
     *
     * @param webhook that notifies Item updates
     */
    private void processItem(Webhook webhook) {

        Item item = itemService.getValidItem(webhook.getItemId());

        log.info("Webhook Acknowledged");
        //TODO: Update Item Database. Update Token Status HERE
        switch (webhook.getWebhookCode()) {
            case ERROR:
                log.info("Issue with Item, Resolved by going through link update");
                break;

            case PENDING_EXPIRATION:
                log.info("The Items access token will expire in 7 days, Resolved by going through link update");
                itemService.setPendingExpiration(item);
                break;

            case USER_PERMISSION_REVOKED:
                log.info("The end user has revoked the permission of access to an Item, Resolved by creating a new Item");
                itemService.expireItem(item);
                break;

            case WEBHOOK_UPDATE_ACKNOWLEDGED:
                log.info("The Item's webhook is updated");
                itemService.setItemToUpdated(item);
                break;

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
        Item item = itemService.getValidItem(itemId);
        TransactionsRefreshRequest transactionsRefreshRequest = new TransactionsRefreshRequest(item.getAccessToken());
        transactionsRefreshRequest.clientId = plaidConfigurationProperties.getClientId();
        transactionsRefreshRequest.secret = plaidConfigurationProperties.getSecret();
        Response<TransactionsRefreshResponse> execute = plaidClient.service().transactionsRefresh(transactionsRefreshRequest).execute();
        if (execute.isSuccessful()) {
            TransactionsRefreshResponse body = execute.body();
            assert body != null;
            log.info("Refresh response: {}", body.getRequestId());
        } else {
            ErrorResponse errorResponse = plaidClient.parseError(execute);
            log.error("Error refreshing transactions: {}", errorResponse.getErrorMessage());
        }


    }
}
