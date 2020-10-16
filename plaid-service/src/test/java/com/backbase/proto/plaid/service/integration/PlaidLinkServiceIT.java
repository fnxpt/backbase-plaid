package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.model.PlaidWebhook;
import com.backbase.proto.plaid.service.PlaidLinkService;
import com.backbase.proto.plaid.service.PlaidTransactionsService;
import com.backbase.proto.plaid.service.PlaidWebhookService;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PlaidApplication.class
)
@Slf4j
public class PlaidLinkServiceIT {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    private PlaidLinkService plaidLinkService;
    @Autowired
    private PlaidTransactionsService plaidTransactionsService;

    @Autowired
    private PlaidWebhookService plaidWebhookService;

    @Test
    public void testGetTransactions(){
        plaidTransactionsService.ingestTransactions("access-testing");
    }

//    @Test
//    public void transactionPagination(){
//        plaidTransactionsService.
//    }

    @Test
    public void testInitialUpdate() {
        PlaidWebhook plaidWebhook = new PlaidWebhook()
            .webhookType(PlaidWebhook.WebhookTypeEnum.TRANSACTIONS)
            .webhookCode(PlaidWebhook.WebhookCodeEnum.INITIAL_UPDATE)
            .itemId("***REMOVED***")
            .newTransactions(393);
        plaidWebhookService.process(plaidWebhook);
    }

    @Test
    public void testDefault() {
        PlaidWebhook plaidWebhook = new PlaidWebhook()
            .webhookType(PlaidWebhook.WebhookTypeEnum.TRANSACTIONS)
            .webhookCode(PlaidWebhook.WebhookCodeEnum.DEFAULT_UPDATE)
            .itemId("***REMOVED***")
            .newTransactions(393);
        plaidWebhookService.process(plaidWebhook);
    }

    @Test
    public void testHistoricalUpdate() {
        PlaidWebhook plaidWebhook = new PlaidWebhook()
            .webhookType(PlaidWebhook.WebhookTypeEnum.TRANSACTIONS)
            .webhookCode(PlaidWebhook.WebhookCodeEnum.HISTORICAL_UPDATE)
            .itemId("***REMOVED***")
            .newTransactions(393);
        plaidWebhookService.process(plaidWebhook);
    }

    @Test
    public void testRemovedTransactions() {
        PlaidWebhook plaidWebhook = new PlaidWebhook()
            .webhookType(PlaidWebhook.WebhookTypeEnum.TRANSACTIONS)
            .webhookCode(PlaidWebhook.WebhookCodeEnum.TRANSACTIONS_REMOVED)
            .itemId("***REMOVED***")
            .removedTransactions(Arrays.asList("transaction1","transaction2"));
        plaidWebhookService.process(plaidWebhook);
    }




    @Test
    public void testIngestAccounts() {
        plaidLinkService.ingestPlaidAccounts(
            "access-testing",
            "lesley.knope",
            "8a808094748c4ca701749668ea030012");
    }

}