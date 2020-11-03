package com.backbase.proto.plaid.service.integration;

import com.backbase.buildingblocks.jwt.internal.authentication.InternalJwtAuthentication;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwt;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwtClaimsSet;
import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.client.model.LinkItem;
import com.backbase.proto.plaid.controller.ItemController;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.Transaction;
import com.backbase.proto.plaid.repository.AccountRepository;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.repository.TransactionRepository;
import com.backbase.proto.plaid.service.AccessTokenService;
import com.backbase.proto.plaid.service.AccountService;
import com.backbase.proto.plaid.service.ItemService;
import com.backbase.proto.plaid.service.LinkService;
import com.backbase.proto.plaid.service.TransactionsService;
import com.backbase.proto.plaid.service.WebhookService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = PlaidApplication.class
)
@Slf4j
public class LinkServiceIT {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Before
    public void setup() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "lesley.knope");
        InternalJwtClaimsSet internalJwtClaimsSet = new InternalJwtClaimsSet(claims);
        InternalJwt internalJwt = new InternalJwt("", internalJwtClaimsSet);
        SecurityContextHolder.getContext().setAuthentication(new InternalJwtAuthentication(internalJwt));
    }


    @Autowired
    private ItemService itemService;

    @Autowired
    private TransactionsService plaidTransactionsService;

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private ItemController itemController;

    @Test
    public void testItemEndpoint() {
        ResponseEntity<List<LinkItem>> items = itemController.getItems("ACTIVE");
        log.info("get Items endpoint response body: {}", items.getBody());
    }


//    @Before
//    public void setUp() throws IOException {
//
//        Response<AccountsBalanceGetResponse> mockResponse = null;
//
//        Mockito.when(accountRepository.deleteAccountsByItemId()).thenReturn(mockResponse);
//
//    }

    @Test
    @Ignore
    public void testGetTransactions() {
//        plaidTransactionsService.ingestHistoricalUpdate("***REMOVED***");
    }


    @Test
    public void testResetDBS() {
        itemService.getAllItems().forEach(item -> itemService.deleteItemFromDBS(item.getItemId()));
//        itemService.deleteItem("***REMOVED***");
//        itemService.deleteItem("***REMOVED***");
    }


    @Test
    public void testIngestItem() {
        Item item = itemRepository.findByItemId("***REMOVED***").get();
        accountService.ingestPlaidAccounts(item, item.getAccessToken(),  "lesley.knope","8a80807b7588a11701758940f08e001b");
    }


    @Test
    public void testGetAllItems() {
        Item item = itemRepository.findByItemId("Ed6bjzrDLJfGvZWwnkQlfxwoNz54B5C97ejBr").orElse(new Item());


        item.setItemId("Ed6bjzrDLJfGvZWwnkQlfxwoNz54B5C97ejBr");
        item.setAccessToken("access-testing");
        item.setCreatedAt(LocalDateTime.now());
        item.setInstitutionId("ins_3");
        item.setCreatedBy("me");
        itemRepository.save(item);

        Item item1 = itemRepository.findByItemId("Ed6bjzr5LJfGvZWwnkQlfxwoNz54B5CGs57sw3").orElse(new Item());
        item1.setItemId("Ed6bjzr5LJfGvZWwnkQlfxwoNz54B5CGs57sw3");
        item1.setAccessToken("access-testing");
        item1.setCreatedAt(LocalDateTime.now());
        item.setInstitutionId("ins_3");
        item1.setCreatedBy("me");
        itemRepository.save(item1);

        Item item2 = itemRepository.findByItemId("Ed6bjzrDLJfdrFD7ba3FQlfxwoNz54B5C9j6ng").orElse(new Item());
        item2.setItemId("Ed6bjzrDLJfdrFD7ba3FQlfxwoNz54B5C9j6ng");
        item2.setAccessToken("access-testing");
        item2.setCreatedAt(LocalDateTime.now());
        item.setInstitutionId("ins_3");
        item2.setCreatedBy("me");
        itemRepository.save(item2);

        // already added 3 under me
        Assert.assertEquals("Expected 6 items: ", 6, itemService.getItemsByUserId("me"));

    }

    @Test
    public void resetSandbox() {
        List<Item> collect = itemRepository.findAll().stream()
            .filter(item -> item.getAccessToken().startsWith("access-sandbox"))
            .collect(Collectors.toList());
        for (Item item : collect) {
            itemService.deleteItem(item.getItemId());
        }
    }


    @Test
    @Ignore
    public void testDeleteItem() {
//        Item item = new Item();
//        item.setItemId("4d6bjNrDLJfGvZWwnkQlfxwoNz54B5C97ejBr");
//        item.setAccessToken("access-testing");
//        item.setCreatedAt(LocalDateTime.now());
//        item.setCreatedBy("me");
//        itemRepository.save(item);

//        itemService.deleteItem("LlpN6poaprSbWAGv69pPH5qAyBgr8EUkZjbyr");

//        Assert.assertFalse(itemRepository.existsByItemId("4d6bjNrDLJfGvZWwnkQlfxwoNz54B5C97ejBr"));
    }


    @Test
    public void testAccountMapping() {


    }

    @Test
    @Ignore
    public void testTransactionMapping() {
        Transaction transaction = new Transaction();

        transactionRepository.save(transaction);


    }

    @Test
    public void testInstitutionMapping() {


    }


}