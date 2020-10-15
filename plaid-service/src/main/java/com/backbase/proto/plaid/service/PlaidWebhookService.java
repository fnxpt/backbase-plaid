package com.backbase.proto.plaid.service;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.model.PlaidWebhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaidWebhookService {

    private final PlaidTransactionsService transactionsService;

    public void process(PlaidWebhook plaidWebhook) {

        switch (plaidWebhook.getWebhookType()) {
            case TRANSACTIONS:
                processTransactions(plaidWebhook);
                break;
            case ITEM:
                processItem(plaidWebhook);
        }


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
