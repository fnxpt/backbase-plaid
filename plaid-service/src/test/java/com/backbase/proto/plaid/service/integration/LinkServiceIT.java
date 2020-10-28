package com.backbase.proto.plaid.service.integration;

import com.backbase.buildingblocks.jwt.internal.authentication.InternalJwtAuthentication;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwt;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwtClaimsSet;
import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.Transaction;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.repository.TransactionRepository;
import com.backbase.proto.plaid.service.AccessTokenService;
import com.backbase.proto.plaid.service.AccountService;
import com.backbase.proto.plaid.service.ItemService;
import com.backbase.proto.plaid.service.TransactionsService;
import com.backbase.proto.plaid.service.WebhookService;
import com.plaid.client.PlaidClient;
import com.plaid.client.response.AccountsBalanceGetResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okio.Timeout;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
        Map<String, Object> claims  = new HashMap<>();
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
    private ItemRepository itemRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccessTokenService accessTokenService;

//    @MockBean
//    private PlaidClient plaidClient;
//
//    @Before
//    public void setUp() throws IOException {
//
//        Response<AccountsBalanceGetResponse> mockResponse = null;
//
//        Mockito.when(plaidClient.service().accountsBalanceGet(any()).execute()).thenReturn(mockResponse);
//
//    }

    @Test
    @Ignore
    public void testGetTransactions(){
//        plaidTransactionsService.ingestHistoricalUpdate("***REMOVED***");
    }


    @Test
    @Ignore
    public void testResetDBS() {
        itemService.getAllItems().forEach(item -> itemService.deleteItemFromDBS(item.getItemId()));
//        itemService.deleteItem("***REMOVED***");
//        itemService.deleteItem("***REMOVED***");
    }

    @Test
    @Ignore
    public void testIngestAll() {
        itemService.getAllItems().forEach(item -> accountService.ingestPlaidAccounts(accessTokenService.getAccessToken(item.getItemId()), "lesley.knope",
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
    @Ignore
    public void testDeleteItem(){
//        Item item = new Item();
//        item.setItemId("4d6bjNrDLJfGvZWwnkQlfxwoNz54B5C97ejBr");
//        item.setAccessToken("access-testing");
//        item.setCreatedAt(LocalDateTime.now());
//        item.setCreatedBy("me");
//        itemRepository.save(item);

        itemService.deleteItem("LlpN6poaprSbWAGv69pPH5qAyBgr8EUkZjbyr");

//        Assert.assertFalse(itemRepository.existsByItemId("4d6bjNrDLJfGvZWwnkQlfxwoNz54B5C97ejBr"));
    }

    @Test
    public void testAccountMapping(){



    }

    @Test
    @Ignore
    public void testTransactionMapping(){
        Transaction transaction = new Transaction();

        transactionRepository.save(transaction);


    }

    @Test
    public void testInstitutionMapping(){

    }





}