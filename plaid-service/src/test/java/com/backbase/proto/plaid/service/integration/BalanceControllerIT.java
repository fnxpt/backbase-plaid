package com.backbase.proto.plaid.service.integration;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.controller.BalanceController;
import com.backbase.proto.plaid.model.Account;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.repository.AccountRepository;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.service.mockserver.plaid.TestMockServer;
import com.backbase.stream.dbs.account.outbound.model.BalanceItemItem;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PlaidApplication.class
)
@Slf4j
public class BalanceControllerIT extends TestMockServer {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    private BalanceController balanceController;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ItemRepository itemRepository;

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
    public void testBalanceController(){

        Account account;

        if (accountRepository.existsByAccountId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA"))
            account = accountRepository.findByAccountId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA");
        else
            account = new Account();

        account.setAccountId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA");
        account.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        account.setMask("0000");
        account.setName("Plaid Checking");
        account.setSubtype("checking");
        account.setType("depository");

        accountRepository.save(account);

        BalanceItemItem expected = new BalanceItemItem()
                .arrangementId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA")
                .availableBalance(BigDecimal.valueOf(110.0));
        Assert.assertEquals("balance not correctly retrieved ",expected,balanceController.getBalance("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA").getBody().get(0));

    }
}
