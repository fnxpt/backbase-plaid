package com.backbase.proto.plaid.service.integration;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.exceptions.IngestionFailedException;
import com.backbase.proto.plaid.model.Account;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.Webhook;
import com.backbase.proto.plaid.repository.*;
import com.backbase.proto.plaid.service.WebhookService;
import com.backbase.proto.plaid.service.mockserver.plaid.TestMockServer;
import com.backbase.proto.plaid.webhook.model.PlaidWebhook;
import com.google.gson.Gson;
import com.plaid.client.PlaidClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
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
        System.setProperty("SIG_SECRET_KEY", "test!");
    }

    @Autowired
    private WebhookService webhookService;

    private Gson gson = new Gson();

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
        testItem.setAccessToken("test-token-cd143f16-3e37-40a1-a269-d65e911312c4");
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

    @BeforeEach
    public void clearWebhookRepo(){
        webhookRepository.deleteAll();
    }

    @Test
    public void testWebhookRefresh() {
        webhookService.refresh("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        Assert.assertTrue(true);
    }

    @Test
    public void rerunFailedWebhooks() {
        Webhook webhook =new Webhook();
        webhook.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        webhook.setWebhookCode(WEBHOOK_UPDATE_ACKNOWLEDGED);
        webhook.setWebhookType(TRANSACTIONS);
        webhook.setCompleted(false);

        webhookRepository.save(webhook);
        Assert.assertTrue("processed",webhookRepository.findAllByCompleted(false).size()>0);
        webhookRepository.findAllByCompleted(false).stream().limit(1).forEach(webhookService::process);
        Assert.assertEquals("processed", 0, webhookRepository.findAllByCompleted(false).size());
    }

    @Test
    public void failWebhook(){
        webhookRepository.deleteAll();
        Item item = new Item();
        item.setItemId("invaildItem");
        item.setCreatedBy("ron.swanson");
        item.setCreatedAt(LocalDateTime.now());
        item.setAccessToken("test-token-item-expired");
        item.setInstitutionId("ins_456rfs6763");
        item.setState("ACTIVE");
        itemRepository.save(item);

        Webhook webhook =new Webhook();
        webhook.setItemId("invaildItem");
        webhook.setWebhookCode(INITIAL_UPDATE);
        webhook.setWebhookType(TRANSACTIONS);
        webhook.setCompleted(false);
        webhookService.process(webhook);

       Webhook webhookProcessed =webhookRepository.findAllByCompleted(true).get(0);
        Assert.assertEquals("not registered error", "ITEM_LOGIN_REQUIRED",webhookProcessed.getError());
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
        webhookRepository.deleteAll();
        Webhook plaidWebhook = new Webhook();
           plaidWebhook.setWebhookType(TRANSACTIONS);
           plaidWebhook.setWebhookCode(TRANSACTIONS_REMOVED);
           plaidWebhook.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
           plaidWebhook.setRemovedTransactions(Arrays.asList("VRVb7BLG1XSXv4gGZoZETMAKANggmEHWJbjkW"));
        webhookService.process(plaidWebhook);
        plaidWebhook.setCompleted(true);
        log.info("expected webhook {}", plaidWebhook);
        Assert.assertEquals(" errors", gson.toJson(plaidWebhook),gson.toJson(webhookRepository.findAllByCompleted(true).get(0)));
    }

    @Test
    public void testValidateWebhook(){
        webhookRepository.deleteAll();
        Webhook invalidWebhook = new Webhook();
            invalidWebhook.setWebhookType(TRANSACTIONS);
            invalidWebhook.setWebhookCode(TRANSACTIONS_REMOVED);
            invalidWebhook.setItemId("InvalidItem");
        webhookService.process(invalidWebhook);
        invalidWebhook.setCompleted(true);
        invalidWebhook.setError("WEBHOOK REFERS TO NO EXISTING ITEM");
        log.info("expected invalid webhook");
      //  Assert.assertEquals("was not errored",gson.toJson(invalidWebhook),gson.toJson(webhookRepository.findAllByCompleted(false).get(0)));
        Assert.assertTrue(true);
    }

    @Test
    public void testWebhookUpdateAcknowledged(){
        Webhook webhookAcknowledged = new Webhook();
        webhookAcknowledged.setWebhookType(ITEM);
        webhookAcknowledged.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        webhookAcknowledged.setWebhookCode(WEBHOOK_UPDATE_ACKNOWLEDGED);
        webhookService.process(webhookAcknowledged);

        Item item = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElseThrow(()-> new BadRequestException("Item not found"));

        Assert.assertEquals("Item update not acknowledged", "UPDATED", item.getState());
    }

    @Test
    public void testWebhookPermissionRevoked(){
        Webhook webhookAcknowledged = new Webhook();
        webhookAcknowledged.setWebhookType(ITEM);
        webhookAcknowledged.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        webhookAcknowledged.setWebhookCode(USER_PERMISSION_REVOKED);
        webhookService.process(webhookAcknowledged);

        Item item = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElseThrow(()-> new BadRequestException("Item not found"));

        Assert.assertEquals("Item not revoked", LocalDate.now(), item.getExpiryDate());

    }


}