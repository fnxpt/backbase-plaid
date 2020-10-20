package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.model.PlaidWebhook;
import com.backbase.proto.plaid.model.Webhook;
import com.backbase.proto.plaid.service.WebhookService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
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
public class WebhookServiceST {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    private WebhookService webhookService;

    @Test
    @Ignore
    public void testWebhookRefresh() {
        webhookService.refresh("***REMOVED***");
//        webhookService.refresh("***REMOVED***");
    }




//    @Test
//    public void testInitialUpdate() {
//        PlaidWebhook plaidWebhook = new PlaidWebhook()
//            .webhookType(PlaidWebhook.WebhookTypeEnum.TRANSACTIONS)
//            .webhookCode(PlaidWebhook.WebhookCodeEnum.INITIAL_UPDATE)
//            .itemId("***REMOVED***")
//            .newTransactions(393);
//        webhookService.process(plaidWebhook);
//    }
//
//    @Test
//    public void testDefault() {
//        PlaidWebhook plaidWebhook = new PlaidWebhook()
//            .webhookType(PlaidWebhook.WebhookTypeEnum.TRANSACTIONS)
//            .webhookCode(PlaidWebhook.WebhookCodeEnum.DEFAULT_UPDATE)
//            .itemId("***REMOVED***")
//            .newTransactions(393);
//        webhookService.process(plaidWebhook);
//    }
//
    @Test
    public void testHistoricalUpdate() {
        Webhook plaidWebhook = new Webhook();
        plaidWebhook.setWebhookType(Webhook.WebhookType.TRANSACTIONS);
        plaidWebhook.setWebhookCode(Webhook.WebhookCode.HISTORICAL_UPDATE);
        plaidWebhook.setWebhookType(Webhook.WebhookType.TRANSACTIONS);
        plaidWebhook.setItemId("***REMOVED***");
        plaidWebhook.setNewTransactions(376);

        webhookService.process(plaidWebhook);
    }
//
//    @Test
//    public void testRemovedTransactions() {
//        PlaidWebhook plaidWebhook = new PlaidWebhook()
//            .webhookType(PlaidWebhook.WebhookTypeEnum.TRANSACTIONS)
//            .webhookCode(PlaidWebhook.WebhookCodeEnum.TRANSACTIONS_REMOVED)
//            .itemId("***REMOVED***")
//            .removedTransactions(Arrays.asList("transaction1","transaction2"));
//        webhookService.process(plaidWebhook);
//    }


}