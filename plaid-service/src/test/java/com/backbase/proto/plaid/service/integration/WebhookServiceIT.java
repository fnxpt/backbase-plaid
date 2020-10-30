package com.backbase.proto.plaid.service.integration;

import com.backbase.buildingblocks.jwt.internal.authentication.InternalJwtAuthentication;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwt;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwtClaimsSet;
import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.model.Webhook;
import com.backbase.proto.plaid.repository.WebhookRepository;
import com.backbase.proto.plaid.service.WebhookService;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static com.backbase.proto.plaid.model.Webhook.WebhookCode.DEFAULT_UPDATE;
import static com.backbase.proto.plaid.model.Webhook.WebhookCode.INITIAL_UPDATE;
import static com.backbase.proto.plaid.model.Webhook.WebhookType.TRANSACTIONS;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = PlaidApplication.class
)
@Slf4j
public class WebhookServiceIT {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }
    @Before
    public void setup() {

    }
    @Autowired
    private WebhookService webhookService;

    @Autowired
    private WebhookRepository webhookRepository;

    @Test
    public void testWebhookRefresh() {
        webhookService.refresh("bvRdEEG4A6f1ov8E9KQDHomX9786b7CmNLz7L");
//        webhookService.refresh("***REMOVED***");
    }

    @Test
    public void rerunFailedWebhooks() {
        webhookRepository.findAllByCompleted(false).stream().limit(1).forEach(webhookService::process);
    }


    @Test
    public void testInitialUpdate() {
        Webhook plaidWebhook = new Webhook();
            plaidWebhook.setWebhookType(TRANSACTIONS);
            plaidWebhook.setWebhookCode(INITIAL_UPDATE);
            plaidWebhook.setItemId("***REMOVED***");
            plaidWebhook.setNewTransactions(393);
        webhookService.process(plaidWebhook);
    }

    @Test
    public void testDefault() {
        Webhook plaidWebhook = new Webhook();
            plaidWebhook.setWebhookType(TRANSACTIONS);
            plaidWebhook.setWebhookCode(DEFAULT_UPDATE);
            plaidWebhook.setItemId("***REMOVED***");
            plaidWebhook.setNewTransactions(393);
        webhookService.process(plaidWebhook);
    }

    @Test
    public void testHistoricalUpdate() {
        Webhook plaidWebhook = new Webhook();
        plaidWebhook.setWebhookType(TRANSACTIONS);
        plaidWebhook.setWebhookCode(Webhook.WebhookCode.HISTORICAL_UPDATE);
        plaidWebhook.setWebhookType(TRANSACTIONS);
        plaidWebhook.setItemId("***REMOVED***");

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