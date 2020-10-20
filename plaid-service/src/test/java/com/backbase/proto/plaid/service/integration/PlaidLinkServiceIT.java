package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.service.AccountService;
import com.backbase.proto.plaid.service.ItemService;
import com.backbase.proto.plaid.service.PlaidLinkService;
import com.backbase.proto.plaid.service.PlaidTransactionsService;
import com.backbase.proto.plaid.service.WebhookService;
import com.netflix.discovery.converters.Auto;
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