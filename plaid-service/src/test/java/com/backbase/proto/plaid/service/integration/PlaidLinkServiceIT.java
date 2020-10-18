package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.service.ItemService;
import com.backbase.proto.plaid.service.PlaidLinkService;
import com.backbase.proto.plaid.service.PlaidTransactionsService;
import com.backbase.proto.plaid.service.WebhookService;
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
    private ItemService itemService;
    @Autowired
    private PlaidTransactionsService plaidTransactionsService;

    @Autowired
    private WebhookService webhookService;

    @Test
    public void testGetTransactions(){
        plaidTransactionsService.ingestHistoricalUpdate("***REMOVED***");
    }


    @Test
    public void testDeleteItem() {
        itemService.deleteItem("***REMOVED***");
        itemService.deleteItem("***REMOVED***");
    }






}