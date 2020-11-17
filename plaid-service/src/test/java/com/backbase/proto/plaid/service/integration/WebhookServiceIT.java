package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.model.Account;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.Webhook;
import com.backbase.proto.plaid.repository.*;
import com.backbase.proto.plaid.service.WebhookService;
import com.backbase.proto.plaid.service.mockserver.plaid.TestMockServer;
import com.backbase.proto.plaid.webhook.model.PlaidWebhook;
import com.plaid.client.PlaidClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.backbase.proto.plaid.model.Webhook.WebhookCode.*;
import static com.backbase.proto.plaid.model.Webhook.WebhookType.ITEM;
import static com.backbase.proto.plaid.model.Webhook.WebhookType.TRANSACTIONS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;


@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = PlaidApplication.class
)
@Slf4j
public class WebhookServiceIT extends TestMockServer {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private WebhookRepository webhookRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private ItemRepository itemRepository;


    @Before
    public void setup() {
        Item testItem = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElse(new Item());
        testItem.setState("ACTIVE");
        testItem.setAccessToken("access-testing");
        testItem.setCreatedAt(LocalDateTime.now());
        testItem.setCreatedBy("lesley.knope");
        testItem.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        testItem.setInstitutionId("ins_456rfs6763");
        itemRepository.save(testItem);

        Institution institution = institutionRepository.getByInstitutionId("ins_456rfs6763").orElse(new Institution());
        institution.setInstitutionId("ins_456rfs6763");
        institution.setName("Bonk");
        institution.setUrl("Bonk.com");
        institution.setFirstRegisteredAt(LocalDateTime.now());
        institution.setFirstCreatedBy("lesley.knope");
        institutionRepository.save(institution);
    }

    @Test
    public void testWebhookRefresh() {
        webhookService.refresh("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        Assert.assertTrue(true);
//        webhookService.refresh("***REMOVED***");
    }

    @Test
    public void rerunFailedWebhooks() {
        Webhook webhook =new Webhook();
        webhook.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        webhook.setWebhookCode(WEBHOOK_UPDATE_ACKNOWLEDGED);
        webhook.setWebhookType(TRANSACTIONS);
        webhook.setCompleted(false);

        webhookRepository.save(webhook);
        Assert.assertTrue("proccessed",webhookRepository.findAllByCompleted(false).size()>0);
        webhookRepository.findAllByCompleted(false).stream().limit(1).forEach(webhookService::process);
        Assert.assertEquals("proccessed", 0, webhookRepository.findAllByCompleted(false).size());
    }



// this is not checked correctly
    @Test
    public void testInitialUpdate() {
        Webhook plaidWebhook = new Webhook();
            plaidWebhook.setWebhookType(TRANSACTIONS);
            plaidWebhook.setWebhookCode(INITIAL_UPDATE);
            plaidWebhook.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
            plaidWebhook.setNewTransactions(393);
        webhookService.process(plaidWebhook);

        int transactions = transactionRepository.findAllByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha", PageRequest.of(0, 10)).get().collect(Collectors.toList()).size();


        Assert.assertEquals("This does not match the expected", 1,transactions);
    }

    @Test
    public void testDefault() {
        Webhook plaidWebhook = new Webhook();
            plaidWebhook.setWebhookType(TRANSACTIONS);
            plaidWebhook.setWebhookCode(DEFAULT_UPDATE);
            plaidWebhook.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
            plaidWebhook.setNewTransactions(393);
        webhookService.process(plaidWebhook);

        int transactions = transactionRepository.findAllByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha", PageRequest.of(0, 10)).get().collect(Collectors.toList()).size();


        Assert.assertEquals("This does not match the expected", 1,transactions);



    }

    @Test
    public void testHistoricalUpdate() {
        Webhook plaidWebhook = new Webhook();
        plaidWebhook.setWebhookType(TRANSACTIONS);
        plaidWebhook.setWebhookCode(Webhook.WebhookCode.HISTORICAL_UPDATE);
        plaidWebhook.setWebhookType(TRANSACTIONS);
        plaidWebhook.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");

        webhookService.process(plaidWebhook);
        int transactions = transactionRepository.findAllByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha", PageRequest.of(0, 10)).get().collect(Collectors.toList()).size();


        Assert.assertEquals("This does not match the expected", 1,transactions);

    }

    @Test
    public void testItemPendingExperation(){
        Webhook plaidWebhook = new Webhook();
        plaidWebhook.setWebhookType(ITEM);
        plaidWebhook.setWebhookCode(Webhook.WebhookCode.PENDING_EXPIRATION);
        plaidWebhook.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");

        webhookService.process(plaidWebhook);
        Item item = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElseThrow(() -> new IllegalStateException("Cannot get item"));
        Assert.assertEquals("match", LocalDate.now().plusDays(7), item.getExpiryDate());
    }



    @Test
    public void testRemovedTransactions() {
        Webhook plaidWebhook = new Webhook();
           plaidWebhook.setWebhookType(TRANSACTIONS);
           plaidWebhook.setWebhookCode(TRANSACTIONS_REMOVED);
           plaidWebhook.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
           plaidWebhook.setRemovedTransactions(Arrays.asList("VRVb7BLG1XSXv4gGZoZETMAKANggmEHWJbjkW"));
        webhookService.process(plaidWebhook);
        Assert.assertTrue("no errors", true);
    }


}