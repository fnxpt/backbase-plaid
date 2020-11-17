package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.repository.AccountRepository;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.service.AccountService;
import com.backbase.proto.plaid.service.mockserver.plaid.TestMockServer;
import com.backbase.stream.legalentity.model.*;
import com.backbase.proto.plaid.model.Account;
import com.google.gson.Gson;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.AccountsBalanceGetRequest;
import com.plaid.client.request.common.Product;
import com.plaid.client.response.ErrorResponse;
import com.plaid.client.response.ItemStatus;
import liquibase.pro.packaged.A;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.plaid.client.request.common.Product.BALANCE;
import static com.plaid.client.request.common.Product.TRANSACTIONS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PlaidApplication.class
)
@Slf4j
public class AccountServiceIT extends TestMockServer {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    private PlaidClient plaidClient;
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private  PlaidConfigurationProperties plaidConfigurationProperties;

    @Autowired
    private ItemRepository itemRepository;
    private Gson gson = new Gson();

    @Before
    public void setUps(){
        Item testItem = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElse(new Item());
        testItem.setState("ACTIVE");
        testItem.setAccessToken("access-testing");
        testItem.setCreatedAt(LocalDateTime.now());
        testItem.setCreatedBy("lesley.knope");
        testItem.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        testItem.setInstitutionId("ins_456rfs6763");
        itemRepository.save(testItem);

    }

    @Test
    public void testmock() throws IOException {
        AccountsBalanceGetRequest request = new AccountsBalanceGetRequest("access-testing");
        log.info("request: {}", gson.toJson(request));
        log.info("account balance response from mock {}", gson.toJson(plaidClient.service().accountsBalanceGet(request).execute().body()));
        Assert.assertEquals(gson.toJson(request),"{\"accessToken\":\"access-testing\",\"clientId\":\"***REMOVED***\",\"secret\":\"***REMOVED***\"}");

    }




    @Test
    public void testDeleteAccounts() {

        Item testItem =itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElse(new Item());
        testItem.setState("ACTIVE");
        testItem.setAccessToken("access-testing");
        testItem.setCreatedAt(LocalDateTime.now());
        testItem.setCreatedBy("me");
        testItem.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        testItem.setInstitutionId("ins_456rfs6763");

        itemRepository.save(testItem);
        Item item = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElseThrow(() -> new NullPointerException());

        accountService.deleteAccountByItemId(item);
        Assert.assertFalse("Mock data was not deleted", accountRepository.existsByAccountId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA"));
    }

    @Test
    public void testAccountMapping() {
        String accessToken = "256gfcs78bga78jn8009";
        ItemStatus itemStatus = mock(ItemStatus.class);
        when(itemStatus.getInstitutionId()).thenReturn("in5");
        when(itemStatus.getItemId()).thenReturn("5789item");
        when(itemStatus.getConsentExpirationTime()).thenReturn(new Date(2021, 7, 25));
        List<Product> products = new ArrayList<>();
        products.add(TRANSACTIONS);
        products.add(BALANCE);
        when(itemStatus.getBilledProducts()).thenReturn(products);
        when(itemStatus.getWebhook()).thenReturn("webhook");
        when(itemStatus.getError()).thenReturn(new ErrorResponse());

        com.backbase.proto.plaid.model.Institution institution = new com.backbase.proto.plaid.model.Institution();
        institution.setFirstCreatedBy("me");
        institution.setFirstRegisteredAt(LocalDateTime.now());
        institution.setInstitutionId("67576987y");
        institution.setLogo("picture");
        institution.setName("bonk");
        institution.setPrimaryColor("Yellow");
        List<String> routingNumbers = new ArrayList<>();
        routingNumbers.add("678");
        routingNumbers.add("798");
        institution.setRoutingNumbers(routingNumbers);
        institution.setUrl("url");

        com.plaid.client.response.Account account = mock(com.plaid.client.response.Account.class);
        when(account.getVerificationStatus()).thenReturn("automatically_verified");
        when(account.getOfficialName()).thenReturn("Plaid Gold Standard 0% Interest Checking");
        when(account.getMask()).thenReturn("0000");
        when(account.getName()).thenReturn("Plaid Checking");
        when(account.getSubtype()).thenReturn("checking");
        when(account.getType()).thenReturn("depository");
        com.plaid.client.response.Account.Balances balances = mock(com.plaid.client.response.Account.Balances.class);
        when(balances.getUnofficialCurrencyCode()).thenReturn(null);
        when(balances.getLimit()).thenReturn(null);
        when(balances.getIsoCurrencyCode()).thenReturn("USD");
        when(balances.getCurrent()).thenReturn(23631.9805);
        when(balances.getAvailable()).thenReturn(null);
        when(account.getBalances()).thenReturn(balances);
        when(account.getAccountId()).thenReturn("blgvvBlXw3cq5GMPwqB6s6q4dLKB9WcVqGDGo");

        com.backbase.stream.legalentity.model.Product expected = new com.backbase.stream.legalentity.model.Product();
        expected.setExternalId("blgvvBlXw3cq5GMPwqB6s6q4dLKB9WcVqGDGo");
        expected.setName("Plaid Checking");
        expected.setBankAlias("Plaid Gold Standard 0% Interest Checking");
        expected.setProductTypeExternalId("current-account");
        expected.setBBAN("0000");
        expected.setCurrency("USD");
        expected.setBookedBalance(new BookedBalance().amount(BigDecimal.valueOf(23631.9805)).currencyCode("USD"));
        Assert.assertEquals("doesn't match", expected, accountService.mapToStream(accessToken, itemStatus, institution, account));

    }

    @Test
    public void getAccountBalanceTest(){
        com.backbase.proto.plaid.model.Account account;

        if (accountRepository.existsByAccountId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA"))
            account = accountRepository.findByAccountId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA");
        else
            account=new Account();

        account.setAccountId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA");
        account.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        account.setMask("0000");
        account.setName("Plaid Checking");
        account.setSubtype("checking");
        account.setType("depository");


        accountRepository.save(account);
        log.info("saved account {}",accountRepository.findByAccountId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA"));
        log.info("accountId {}", accountRepository.findByAccountId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA").getItemId());


        List<String> accountNumber = new ArrayList<>();
        accountNumber.add("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA");
        accountService.getAccountBalance(accountNumber);
        Assert.assertEquals("account balance not gotten",1,        accountService.getAccountBalance(accountNumber).size());

    }

    @Test
    public void testIngestAccounts() {
 //       when(plaidConfigurationProperties.getAccounts().getAccountTypeMap().containsKey("key")).thenReturn(false);
//        plaidConfigurationProperties.setAccounts(new PlaidConfigurationProperties.AccountConfigurationProperties());
        Item testItem = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElse(new Item());
        testItem.setState("ACTIVE");
        testItem.setAccessToken("access-testing");
        testItem.setCreatedAt(LocalDateTime.now());
        testItem.setCreatedBy("me");
        testItem.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        testItem.setInstitutionId("ins_456rfs6763");

        itemRepository.save(testItem);

        Item item = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElseThrow(() -> new NullPointerException("null"));

        accountService.ingestPlaidAccounts(item,
                "access-testing",
                "me",
                "8q73649283472");
        Assert.assertTrue("Mock data was not saved", accountRepository.existsByAccountId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA"));
    }

}