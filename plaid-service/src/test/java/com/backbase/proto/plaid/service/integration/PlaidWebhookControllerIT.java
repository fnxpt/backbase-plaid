package com.backbase.proto.plaid.service.integration;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.controller.PlaidWebHookController;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.repository.WebhookRepository;
import com.backbase.proto.plaid.service.mockserver.plaid.TestMockServer;
import com.backbase.proto.plaid.webhook.model.InlineObject;
import com.backbase.proto.plaid.webhook.model.PlaidWebhook;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PlaidApplication.class
)
@Slf4j
public class PlaidWebhookControllerIT extends TestMockServer {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    private PlaidWebHookController plaidWebHookController;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private WebhookRepository webhookRepository;

    @Before
    public void setUps() {
        createItem();
    }

    private Item createItem(){

        Item testItem = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElse(new Item());
        testItem.setState("ACTIVE");
        testItem.setAccessToken("access-testing");
        testItem.setCreatedAt(LocalDateTime.now());
        testItem.setCreatedBy("lesley.knope");
        testItem.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        testItem.setInstitutionId("ins_456rfs6763");
        itemRepository.save(testItem);

        return itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElseThrow(()-> new BadRequestException("Item count not be saved"));
    }

    @Test
    public void processWebhookTest(){
        Assert.assertEquals("webhook repo not empty", 0 ,webhookRepository.findAllByCompleted(true).size() );
        PlaidWebhook plaidWebhook = new PlaidWebhook()
                .webhookCode("PENDING_EXPIRATION")
                .webhookType("ITEM")
                .itemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        plaidWebHookController.processWebHook("WGYJu6gjhA6r6ygSGYI6556456gvgha",plaidWebhook);

        Assert.assertEquals("webhook not processed", 1, webhookRepository.findAllByCompleted(true).size());
    }

    @Test
    public void refreshTransactionsTest(){
        plaidWebHookController.refreshTransactions("WGYJu6gjhA6r6ygSGYI6556456gvgha",new InlineObject().all(true));
        Assert.assertTrue(true);
    }
}
