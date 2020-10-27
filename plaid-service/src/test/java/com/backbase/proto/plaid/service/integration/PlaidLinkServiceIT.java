package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.service.AccountService;
import com.backbase.proto.plaid.service.ItemService;
import com.backbase.proto.plaid.service.PlaidLinkService;
import com.backbase.proto.plaid.service.PlaidTransactionsService;
import com.backbase.proto.plaid.service.WebhookService;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;


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
    private ItemService itemService;
    @Autowired
    private PlaidTransactionsService plaidTransactionsService;

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void testGetTransactions(){
        plaidTransactionsService.ingestHistoricalUpdate("***REMOVED***");
    }


    @Test
    public void testResetDBS() {
        itemService.getAllItems().forEach(item -> itemService.deleteItemFromDBS(item.getItemId()));
//        itemService.deleteItem("***REMOVED***");
//        itemService.deleteItem("***REMOVED***");
    }

    @Test
    public void testIngestAll() {
        itemService.getAllItems().forEach(item -> accountService.ingestPlaidAccounts(itemService.getAccessToken(item.getItemId()), "lesley.knope",
            "8a808094748c4ca701749668ea030012"));
//        itemService.deleteItem("***REMOVED***");
//        itemService.deleteItem("***REMOVED***");
    }

    @Test
    public void testGetAllItems() {
        Item item = new Item();
        item.setItemId("Ed6bjNrDLJfGvZWwnkQlfxwoNz54B5C97ejBr");
        item.setAccessToken("access-testing");
        item.setCreatedAt(LocalDateTime.now());
        item.setCreatedBy("me");
        itemRepository.save(item);

        Item item1 = new Item();
        item1.setItemId("***REMOVED***");
        item1.setAccessToken("access-testing");
        item1.setCreatedAt(LocalDateTime.now());
        item1.setCreatedBy("me");
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setItemId("***REMOVED***");
        item2.setAccessToken("access-testing");
        item2.setCreatedAt(LocalDateTime.now());
        item2.setCreatedBy("me");
        itemRepository.save(item2);

        List<Item> me = itemService.getItemsByUserId("me");

        Assert.assertEquals("Expected 3 items: ", 3, me.size());


    }

    @Test
    public void test


    @Test
    public void testUnlinkItems() {
        itemService.getAllItems().forEach(item -> {
            try {
                itemService.deleteItem(item.getItemId());
            } catch (Exception exception) {
                log.error("Failed to deleteItem: {}", item.getItemId(), exception);

            }
        });
    }




}