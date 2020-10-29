package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.repository.AccountRepository;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.service.AccountService;
import com.plaid.client.request.common.Product;
import com.plaid.client.response.Account;
import com.plaid.client.response.AccountsBalanceGetResponse;
import com.plaid.client.response.ErrorResponse;
import com.plaid.client.response.ItemStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.plaid.client.request.common.Product.TRANSACTIONS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PlaidApplication.class
)
@Slf4j
public class AccountServiceIT {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    @MockBean
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;


    @Before
    public void setUp() {
        //mocked item
        List<Product> availableProducts = new ArrayList<>();
        availableProducts.add(TRANSACTIONS);
        List<Product> billedProducts = new ArrayList<>();
        billedProducts.add(TRANSACTIONS);
        ErrorResponse error = null;
        String institutionId = "ins_117650";
        String itemId = "DWVAAPWq4RHGlEaNyGKRTAnPLaEmo8Cvq7na6";
        String webhook = "https://www.genericwebhookurl.com/webhook";
        Date consentExpirationTime = null;

        ItemStatus mockItem = mock(ItemStatus.class);

        when(mockItem.getAvailableProducts()).thenReturn(availableProducts);
        when(mockItem.getBilledProducts()).thenReturn(billedProducts);
        when(mockItem.getConsentExpirationTime()).thenReturn(consentExpirationTime);
        when(mockItem.getError()).thenReturn(error);
        when(mockItem.getInstitutionId()).thenReturn(institutionId);
        when(mockItem.getItemId()).thenReturn(itemId);
        when(mockItem.getWebhook()).thenReturn(webhook);

        //mocked balance
        Double available = 100d;
        Double current = 110d;
        Double limit = null;
        String isoCurrencyCode = "USD";
        String unofficialCurrencyCode = null;

        //mocked account
        String accountId = "blgvvBlXw3cq5GMPwqB6s6q4dLKB9WcVqGDGo";
        String type = "depository";
        String subtype = "checking";
        String name = "Plaid Checking";
        String mask = "0000";
        String officialName = "Plaid Gold Standard 0% Interest Checking" ;
        String verificationStatus = null;

        List<Account> mockAccounts = new ArrayList<>();

        Account mockAccount = mock(Account.class);
        mockAccounts.add(mockAccount);
        when(mockAccount.getAccountId()).thenReturn(accountId);
        when(mockAccount.getBalances().getAvailable()).thenReturn(available);
        when(mockAccount.getBalances().getCurrent()).thenReturn(current);
        when(mockAccount.getBalances().getIsoCurrencyCode()).thenReturn(isoCurrencyCode);
        when(mockAccount.getBalances().getLimit()).thenReturn(limit);
        when(mockAccount.getBalances().getUnofficialCurrencyCode()).thenReturn(unofficialCurrencyCode);
        when(mockAccount.getType()).thenReturn(type);
        when(mockAccount.getSubtype()).thenReturn(subtype);
        when(mockAccount.getName()).thenReturn(name);
        when(mockAccount.getMask()).thenReturn(mask);
        when(mockAccount.getOfficialName()).thenReturn(officialName);
        when(mockAccount.getVerificationStatus()).thenReturn(verificationStatus);

        //mocked Get accounts response
        AccountsBalanceGetResponse mockResponse = mock(AccountsBalanceGetResponse.class);

        when(mockResponse.getAccounts()).thenReturn(mockAccounts);
        when(mockResponse.getItem()).thenReturn(mockItem);

        //mocked account service
        when(accountService.requestPlaidAccounts(any())).thenReturn(mockResponse);
    }

    @Test
    @Ignore
    public void testIngestAccounts() {
        accountService.ingestPlaidAccounts(
                "access-testing",
                "lesley.knope",
                "8a808094748c4ca701749668ea030012");
        Assert.assertTrue("Mock data was not saved",accountRepository.existsByAccountId("blgvvBlXw3cq5GMPwqB6s6q4dLKB9WcVqGDGo"));
        // check account repo for these accounts
    }


    @Test
    public void testDeleteAccounts() {
//        accountService.deleteAccountByItemId("LlpN6poaprSbWAGv69pPH5qAyBgr8EUkZjbyr");
    }

}