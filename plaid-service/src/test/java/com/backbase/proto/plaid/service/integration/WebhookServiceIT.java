package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.model.Account;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.Webhook;
import com.backbase.proto.plaid.repository.AccountRepository;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.repository.TransactionRepository;
import com.backbase.proto.plaid.repository.WebhookRepository;
import com.backbase.proto.plaid.service.WebhookService;
import com.plaid.client.PlaidClient;
import com.plaid.client.response.TransactionsGetResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.backbase.proto.plaid.model.Webhook.WebhookCode.DEFAULT_UPDATE;
import static com.backbase.proto.plaid.model.Webhook.WebhookCode.INITIAL_UPDATE;
import static com.backbase.proto.plaid.model.Webhook.WebhookType.ITEM;
import static com.backbase.proto.plaid.model.Webhook.WebhookType.TRANSACTIONS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = PlaidApplication.class
)
@Slf4j
public class WebhookServiceIT {

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
    private ItemRepository itemRepository;

    @Autowired
    private PlaidClient plaidClient;

    @Before
    public void setup() throws IOException {
        PlaidClient.Builder mockBuilder;
        mockBuilder = mock(PlaidClient.Builder.class);
        PlaidClient mockPlaidClient = mock(PlaidClient.class);
        when(mockBuilder.build()).thenReturn(mockPlaidClient);
        when(PlaidClient.newBuilder()).thenReturn(mockBuilder);
// ask bart
        Response<TransactionsGetResponse> response = mock(Response.class);
        when(mockPlaidClient.service().transactionsGet(any()).execute()).thenReturn(response);
        TransactionsGetResponse responseBody = mock(TransactionsGetResponse.class);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.getItem().getInstitutionId()).thenReturn("ins_3");

        TransactionsGetResponse.Transaction mockTransaction= mock(TransactionsGetResponse.Transaction.class);

        when(mockTransaction.getAccountId()).thenReturn("BxBXxLj1m4HMXBm9WZZmCWVbPjX16EHwv99vp");
        when(mockTransaction.getAmount()).thenReturn(2307.21);
        when(mockTransaction.getIsoCurrencyCode()).thenReturn("USD");
        List<String> categories = new ArrayList<>();
        categories.add("Shops");
        categories.add("Computers and Electronics");
        when(mockTransaction.getCategory()).thenReturn(categories);
        when(mockTransaction.getCategoryId()).thenReturn("19013000");
        when(mockTransaction.getDate()).thenReturn("2017-01-29");
        when(mockTransaction.getAuthorizedDate()).thenReturn("2017-01-27");

        TransactionsGetResponse.Transaction.Location location = mock(TransactionsGetResponse.Transaction.Location.class);
        when(location.getAddress()).thenReturn("300 Post St");
        when(location.getCity()).thenReturn("San Francisco");
        when(location.getCountry()).thenReturn("US");
        when(location.getRegion()).thenReturn("CA");
        when(location.getPostalCode()).thenReturn("94108");
        when(location.getLat()).thenReturn(40.740352);
        when(location.getLon()).thenReturn(-74.001761);
        when(location.getStoreNumber()).thenReturn("1235");

        when(mockTransaction.getLocation()).thenReturn(location);
        when(mockTransaction.getName()).thenReturn("Apple Store");
        when(mockTransaction.getMerchantName()).thenReturn("Apple");

        TransactionsGetResponse.Transaction.PaymentMeta mockPaymentMeta = mock(TransactionsGetResponse.Transaction.PaymentMeta.class);
        when(mockTransaction.getPaymentMeta()).thenReturn(mockPaymentMeta);

        when(mockTransaction.getPaymentChannel()).thenReturn("in_store");
        when(mockTransaction.getPending()).thenReturn(false);
        when(mockTransaction.getPendingTransactionId()).thenReturn(null);
        when(mockTransaction.getAccountOwner()).thenReturn(null);
        when(mockTransaction.getTransactionId()).thenReturn("lPNjeW1nR6CDn5okmGQ6hEpMo4lLNoSrzqDje");
        when(mockTransaction.getTransactionCode()).thenReturn(null);
        when(mockTransaction.getTransactionType()).thenReturn("place");

        List<TransactionsGetResponse.Transaction> mockTransactions = new ArrayList<>();
        mockTransactions.add(mockTransaction);
        when(responseBody.getTransactions()).thenReturn(mockTransactions);


    }

    @Test
    public void testWebhookRefresh() {
        webhookService.refresh("bvRdEEG4A6f1ov8E9KQDHomX9786b7CmNLz7L");
//        webhookService.refresh("***REMOVED***");
    }

    @Test
    public void rerunFailedWebhooks() {
        webhookRepository.findAllByCompleted(false).stream().limit(1).forEach(webhookService::process);
    }



// this is not checked correctly
    @Test
    public void testInitialUpdate() {
        Webhook plaidWebhook = new Webhook();
            plaidWebhook.setWebhookType(TRANSACTIONS);
            plaidWebhook.setWebhookCode(INITIAL_UPDATE);
            plaidWebhook.setItemId("***REMOVED***");
            plaidWebhook.setNewTransactions(393);
        webhookService.process(plaidWebhook);

        List<String> accounts = accountRepository.findAllByItemId("***REMOVED***").stream().map(Account::getAccountId).collect(Collectors.toList());
        int transactions=0;
        for(int i=0; i<accounts.size();i++ ){
            transactions += (transactionRepository.findAllByAccountId(accounts.get(i))).size();
        }

        Assert.assertEquals("This does not match the expected", 7,transactions);
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

    @Test
    public void testItemPendingExperation(){
        Webhook plaidWebhook = new Webhook();
        plaidWebhook.setWebhookType(ITEM);
        plaidWebhook.setWebhookCode(Webhook.WebhookCode.PENDING_EXPIRATION);
        plaidWebhook.setItemId("***REMOVED***");

        webhookService.process(plaidWebhook);
        Item item = itemRepository.findByItemId("***REMOVED***").orElseThrow(() -> new IllegalStateException("Cannot get item"));
        Assert.assertEquals("match", LocalDate.now().plusDays(7), item.getExpiryDate());
    }

    @Test
    private void testItemUserPermissionRevoked(){
        Webhook plaidWebhook = new Webhook();
        plaidWebhook.setWebhookType(ITEM);
        plaidWebhook.setWebhookCode(Webhook.WebhookCode.USER_PERMISSION_REVOKED);
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