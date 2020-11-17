package com.backbase.proto.plaid.service.integration;

import com.backbase.buildingblocks.jwt.internal.authentication.InternalJwtAuthentication;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwt;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwtClaimsSet;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.transaction.presentation.service.api.TransactionsApi;
import com.backbase.dbs.transaction.presentation.service.model.TransactionIds;
import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.client.model.*;
import com.backbase.proto.plaid.controller.ItemController;
import com.backbase.proto.plaid.exceptions.IngestionFailedException;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.Transaction;
import com.backbase.proto.plaid.repository.AccountRepository;
import com.backbase.proto.plaid.repository.InstitutionRepository;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.repository.TransactionRepository;
import com.backbase.proto.plaid.service.AccessTokenService;
import com.backbase.proto.plaid.service.AccountService;
import com.backbase.proto.plaid.service.ItemService;
import com.backbase.proto.plaid.service.LinkService;
import com.backbase.proto.plaid.service.TransactionsService;
import com.backbase.proto.plaid.service.WebhookService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.backbase.proto.plaid.service.mockserver.plaid.TestMockServer;
import com.backbase.stream.product.service.ArrangementService;
import com.plaid.client.PlaidClient;
import com.plaid.client.response.AccountsBalanceGetResponse;
import com.plaid.client.response.ItemRemoveResponse;
import com.plaid.client.response.TransactionsGetResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import retrofit2.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = PlaidApplication.class
)
@Slf4j
public class LinkServiceIT extends TestMockServer {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    private ItemService itemService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    private TransactionsService plaidTransactionsService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private InstitutionRepository institutionRepository;


    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ItemController itemController;

    @Autowired
    private LinkService linkService;

    @Before
    public void setup() throws IOException {
        //AUTHENTICATION
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "lesley.knope");
        claims.put("leid","8q73649283472");
        InternalJwtClaimsSet internalJwtClaimsSet = new InternalJwtClaimsSet(claims);
        InternalJwt internalJwt = new InternalJwt("", internalJwtClaimsSet);
        SecurityContextHolder.getContext().setAuthentication(new InternalJwtAuthentication(internalJwt));

        Item testItem = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElse(new Item());
        testItem.setState("ACTIVE");
        testItem.setAccessToken("access-testing");
        testItem.setCreatedAt(LocalDateTime.now());
        testItem.setCreatedBy("lesley.knope");
        testItem.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        testItem.setInstitutionId("ins_456rfs6763");
        itemRepository.save(testItem);

        Institution institution = institutionRepository.getByInstitutionId("ins_456rfs6763").orElse(new Institution());
        institution.setInstitutionId("ins_456rfs6763");
        institution.setName("Bonk");
        institution.setUrl("Bonk.com");
        institution.setFirstRegisteredAt(LocalDateTime.now());
        institution.setFirstCreatedBy("lesley.knope");
        institutionRepository.save(institution);

    }




    @Test
    public void testItemEndpoint() {
//        Item testItem = new Item();
//        testItem.setState("ACTIVE");
//        testItem.setAccessToken("access-testing");
//        testItem.setCreatedAt(LocalDateTime.now());
//        testItem.setCreatedBy("lesley.knope");
//        testItem.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
//        testItem.setInstitutionId("ins_456rfs6763");
//
//        Institution institution = new Institution();
//        institution.setInstitutionId("ins_456rfs6763");
//        institution.setName("Bonk");
//        institution.setUrl("Bonk.com");
//        institution.setFirstRegisteredAt(LocalDateTime.now());
//        institution.setFirstCreatedBy("lesley.knope");
//
//        institutionRepository.save(institution);
//        itemRepository.save(testItem);


        ResponseEntity<List<LinkItem>> items = itemController.getItems("ACTIVE");
        log.info("get Items endpoint response body: {}", items.getBody());

        LinkItem expectedLinkItem = new LinkItem().itemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").accounts(new ArrayList<String>()).institutionName("Bonk");

        Assert.assertEquals("number of items doesn't match", 1, items.getBody().size());
        Assert.assertEquals("items don't match", expectedLinkItem, items.getBody().get(0));


    }

    @Test
    public void testGetTransactions() throws IngestionFailedException {
//        Item item = new Item();
//        item.setItemId("7387bfejh6ds98n2gc33");
//        item.setInstitutionId("ins7");
//        item.setCreatedBy("me");
//        item.setCreatedAt(LocalDateTime.now());
//        item.setAccessToken("access-testing");
//
//        itemRepository.save(item);

        Item item = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElseThrow(() -> new BadRequestException("Item not found"));

        plaidTransactionsService.ingestHistoricalUpdate(item);
        Assert.assertTrue("Transaction not present", transactionRepository.existsByTransactionId("VRVb7BLG1XSXv4gGZoZETMAKANggmEHWJbjkW"));
    }


    @Test
    public void testResetDBS() {
        itemService.deleteItem("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        Item item = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElseThrow(() -> new BadRequestException("Item not found"));
        Assert.assertEquals("Status not changed","DELETED",item.getState());
    }

    @Test
    public void testIngestItem() {
        Item item = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").get();
        accountService.ingestPlaidAccounts(item, item.getAccessToken(),  "lesley.knope","8q73649283472");

        Assert.assertTrue("account not ingested", accountRepository.existsByAccountId("DZpP9JqjRrSNnpVZArAyslbwnvQq3Btv8m9rA"));

    }


    @Test
    public void testGetAllItems() {
        Item item = new Item();
        item.setItemId("Ed6bjzrDLJfGvZWwnkQlfxwoNz54B5C97ejBr");
        item.setAccessToken("access-testing");
        item.setCreatedAt(LocalDateTime.now());
        item.setInstitutionId("ins_3");
        item.setCreatedBy("me");
        itemRepository.save(item);

        Item item1 = new Item();
        item1.setItemId("Ed6bjzr5LJfGvZWwnkQlfxwoNz54B5CGs57sw3");
        item1.setAccessToken("access-testing");
        item1.setCreatedAt(LocalDateTime.now());
        item1.setInstitutionId("ins_3");
        item1.setCreatedBy("me");
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setItemId("Ed6bjzrDLJfdrFD7ba3FQlfxwoNz54B5C9j6ng");
        item2.setAccessToken("access-testing");
        item2.setCreatedAt(LocalDateTime.now());
        item2.setInstitutionId("ins_3");
        item2.setCreatedBy("me");
        itemRepository.save(item2);

        Institution institution = new Institution();
        institution.setInstitutionId("ins_3");
        institution.setName("Bank");
        institution.setUrl("Bank.com");
        institution.setFirstRegisteredAt(LocalDateTime.now());
        institution.setFirstCreatedBy("me");
        institutionRepository.save(institution);


        // already added 3 under me
        Assert.assertEquals("Expected 3 items: ", 3, itemService.getItemsByUserId("me").size());

    }

//    @Test
//    public void resetSandbox() {
//        List<Item> collect = itemRepository.findAll().stream()
//            .filter(item -> item.getAccessToken().startsWith("access-sandbox"))
//            .collect(Collectors.toList());
//        for (Item item : collect) {
//            itemService.deleteItem(item.getItemId());
//        }
//    }


//    @Test
//    @Ignore
//    public void testDeleteItem() {
//        Item item = new Item();
//        item.setItemId("4d6bjNrDLJfGvZWwnkQlfxwoNz54B5C97ejBr");
//        item.setAccessToken("access-testing");
//        item.setCreatedAt(LocalDateTime.now());
//        item.setCreatedBy("me");
//        itemRepository.save(item);
//        itemService.deleteItem("LlpN6poaprSbWAGv69pPH5qAyBgr8EUkZjbyr");
//        Assert.assertFalse(itemRepository.existsByItemId("4d6bjNrDLJfGvZWwnkQlfxwoNz54B5C97ejBr"));
//    }



    @Test
    public void testCreatePlaidLink(){
        PlaidLinkRequest plaidLinkRequest = new PlaidLinkRequest().language("en").name("bart");
       // linkService.createPlaidLink(plaidLinkRequest);
        PlaidLinkResponse plaidLink = linkService.createPlaidLink(plaidLinkRequest);
        Assert.assertEquals("not expected link", "link-sandbox-b61203e9-2455-4fba-9cea-a438812938bb", plaidLink.getToken());
    }

    @Test
    public void testCreateItem(){
        itemRepository.deleteAll();
        PlaidInstitution institution = new PlaidInstitution().institutionId("id_6g7").name("pete");
        linkService.setPublicAccessToken(new SetAccessTokenRequest()
                .publicToken("public-token-1gywu6twqej")
                .metadata(new Metadata()
                        .institution(institution)
                        .account(new PlaidAccount()
                                .id("567gyj7u6igjgUguy")
                                .name("account")
                                .mask("0000")
                                .type("depository")
                                .subtype("checking"))));

        Item item = itemRepository.findByItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha").orElseThrow(()->new BadRequestException("Item not found"));

        Assert.assertEquals("item succesfully created",LocalDateTime.now().toLocalDate(),item.getCreatedAt().toLocalDate());


    }




}