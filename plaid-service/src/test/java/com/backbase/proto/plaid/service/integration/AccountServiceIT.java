package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.mapper.AccountMapper;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.repository.AccountRepository;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.service.AccountService;
import com.backbase.proto.plaid.service.TransactionsService;
import com.backbase.stream.legalentity.model.AvailableBalance;
import com.plaid.client.request.common.Product;
import com.plaid.client.response.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static com.plaid.client.request.common.Product.BALANCE;
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


    private AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);
    @Autowired
    private AccountService accountService;
    @MockBean
    @Autowired
    private AccountRepository accountRepository;
    @MockBean
    @Autowired
    private TransactionsService plaidTransactionsService;
    @Autowired
    private ItemRepository itemRepository;


    public void setUp() {
        //mocked item
        List<Product> availableProducts = new ArrayList<>();
        availableProducts.add(TRANSACTIONS);
        List<Product> billedProducts = new ArrayList<>();
        billedProducts.add(TRANSACTIONS);
        String institutionId = "ins_117650";
        String itemId = "DWVAAPWq4RHGlEaNyGKRTAnPLaEmo8Cvq7na6";
        String webhook = "https://www.genericwebhookurl.com/webhook";

        ItemStatus mockItem = mock(ItemStatus.class);

        when(mockItem.getAvailableProducts()).thenReturn(availableProducts);
        when(mockItem.getBilledProducts()).thenReturn(billedProducts);
        when(mockItem.getInstitutionId()).thenReturn(institutionId);
        when(mockItem.getItemId()).thenReturn(itemId);
        when(mockItem.getWebhook()).thenReturn(webhook);

        //mocked balance
        Double available = 100.0;
        Double current = 110.0;
        String isoCurrencyCode = "USD";

        //mocked account
        String accountId = "blgvvBlXw3cq5GMPwqB6s6q4dLKB9WcVqGDGo";
        String type = "depository";
        String subtype = "checking";
        String name = "Plaid Checking";
        String mask = "0000";
        String officialName = "Plaid Gold Standard 0% Interest Checking" ;

        List<Account> mockAccounts = new ArrayList<>();

        Account mockAccount = mock(Account.class);
        mockAccounts.add(mockAccount);
        when(mockAccount.getAccountId()).thenReturn(accountId);
        when(mockAccount.getBalances().getAvailable()).thenReturn(available);
        when(mockAccount.getBalances().getCurrent()).thenReturn(current);
        when(mockAccount.getBalances().getIsoCurrencyCode()).thenReturn(isoCurrencyCode);
        when(mockAccount.getType()).thenReturn(type);
        when(mockAccount.getSubtype()).thenReturn(subtype);
        when(mockAccount.getName()).thenReturn(name);
        when(mockAccount.getMask()).thenReturn(mask);
        when(mockAccount.getOfficialName()).thenReturn(officialName);

        //mocked Get accounts response
        AccountsBalanceGetResponse mockResponse = mock(AccountsBalanceGetResponse.class);

        when(mockResponse.getAccounts()).thenReturn(mockAccounts);
        when(mockResponse.getItem()).thenReturn(mockItem);

        //mocked account service
        when(accountService.requestPlaidAccounts(any())).thenReturn(mockResponse);


    }

    @Test
    public void testIngestAccounts() {

        Item item = itemRepository.findByItemId("***REMOVED***").orElseThrow(() -> new NullPointerException());

        accountService.ingestPlaidAccounts(item,
                item.getAccessToken(),
                item.getCreatedBy(),
                "8a80809475c1b3af0175c1c8f679000b");
    }


    @Test
    public void testDeleteAccounts() {
//        accountService.deleteAccountByItemId("LlpN6poaprSbWAGv69pPH5qAyBgr8EUkZjbyr");
    }

    @Test
    public void testAccountMapping(){
        String accessToken= "256gfcs78bga78jn8009";
        ItemStatus itemStatus = mock(ItemStatus.class);
        when(itemStatus.getInstitutionId()).thenReturn("in5");
        when(itemStatus.getItemId()).thenReturn("5789item");
        when(itemStatus.getConsentExpirationTime()).thenReturn(new Date(2021,7,25));
        List<Product> products= new ArrayList<>();
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

        Account account = mock(Account.class);
        when(account.getVerificationStatus()).thenReturn("automatically_verified");
        when(account.getOfficialName()).thenReturn("Plaid Gold Standard 0% Interest Checking");
        when(account.getMask()).thenReturn("0000");
        when(account.getName()).thenReturn("Plaid Checking");
        when(account.getSubtype()).thenReturn("checking");
        when(account.getType()).thenReturn("depository");
        Account.Balances balances = mock(Account.Balances.class);
        when(balances.getUnofficialCurrencyCode()).thenReturn(null);
        when(balances.getLimit()).thenReturn(null);
        when(balances.getIsoCurrencyCode()).thenReturn("USD");
        when(balances.getCurrent()).thenReturn(23631.9805);
        when(balances.getAvailable()).thenReturn(null);
        when(account.getBalances()).thenReturn(balances);
        when(account.getAccountId()).thenReturn("blgvvBlXw3cq5GMPwqB6s6q4dLKB9WcVqGDGo");

        com.backbase.stream.legalentity.model.Product expected = new com.backbase.stream.legalentity.model.Product();
        Map<String, Object> additions = new HashMap<>();
        additions.put("plaidInstitutionId","67576987y" );
        additions.put("plaidAccountOfficialName", "Plaid Gold Standard 0% Interest Checking");
        additions.put("institutionName", "bonk");
        additions.put("institutionLogo", "picture");
        expected.additions(additions);
        expected.setExternalId("blgvvBlXw3cq5GMPwqB6s6q4dLKB9WcVqGDGo");
        expected.setName("Plaid Checking");
        expected.setBankAlias("Plaid Checking");
        expected.setProductTypeExternalId("67576987y-checking");
        expected.setBBAN("0000");
        expected.setAvailableBalance(new AvailableBalance().amount(BigDecimal.valueOf(23631.9805)).currencyCode("USD"));
        Assert.assertEquals("doesn' match", expected, accountService.mapToStream(accessToken,itemStatus,institution,account));

    }

}