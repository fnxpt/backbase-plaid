package com.backbase.proto.plaid.service.integration;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.controller.TransactionEnrichController;
import com.backbase.proto.plaid.model.Location;
import com.backbase.proto.plaid.model.PaymentMeta;
import com.backbase.proto.plaid.repository.TransactionRepository;
import com.backbase.proto.plaid.service.mockserver.plaid.TestMockServer;
import com.backbase.proto.plaid.service.model.EnrichmentResult;
import com.backbase.proto.plaid.service.model.Merchant;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.backbase.proto.plaid.service.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PlaidApplication.class
)
@Slf4j

public class TransactionEnrichControllerIT extends TestMockServer {

    static {
        System.setProperty("SIG_SECRET_KEY", "test!");
    }

    @Autowired
    private TransactionEnrichController transactionEnrichController;

    @Autowired
    private TransactionRepository transactionRepository;

    private Gson gson = new Gson();

    @Test
    public void transactionEnrichControllerTest(){
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(createTransaction());

        saveTransaction();
        EnrichmentResult expected = new EnrichmentResult().categoryId("22001000").description("United Airlines").merchant(new Merchant().name("United Airlines")).id("8a80837e75930a30017598567e4007fe");

        log.info("response {}",gson.toJson(transactionEnrichController.enrichTransactions(transactions)));

        Assert.assertEquals("transaction not enriched", expected.getCategoryId(), transactionEnrichController.enrichTransactions(transactions).getBody().get(0).getCategoryId());
        Assert.assertEquals("transaction not enriched", expected.getDescription(), transactionEnrichController.enrichTransactions(transactions).getBody().get(0).getDescription());
        Assert.assertEquals("transaction not enriched", expected.getMerchant().getName(), transactionEnrichController.enrichTransactions(transactions).getBody().get(0).getMerchant().getName());

    }

    private Transaction createTransaction(){
        Transaction transaction = new Transaction()
                .id("8a80837e75930a30017598567e4007fe")
                .description("United Airlines")
                .amount("500.0")
                .transactionType(Transaction.TransactionTypeEnum.DEBIT);

        return transaction;

    }
    private com.backbase.proto.plaid.model.Transaction saveTransaction(){
        com.backbase.proto.plaid.model.Transaction transaction = transactionRepository.findByTransactionId("VRVb7BLG1XSXv4gGZoZETMAKANggmEHWJbjkW").orElse(new com.backbase.proto.plaid.model.Transaction());

        transaction.setAccountId("5K3v1kbGxwSLQ6me9W9xfBMkLqNpXmuZDzeEB");
        transaction.setAccountOwner(null);
        transaction.setAmount((double) 500);
        transaction.setAuthorizedDate(null);
        List<String> category = new ArrayList<>();
        category.add("Travel");
        category.add("Airlines and Aviation Services");
        transaction.setCategory(category);
        transaction.setCategoryId("22001000");
        transaction.setDate(LocalDate.parse("2019-05-06"));
        transaction.setIsoCurrencyCode("USD");
        transaction.setLocation(new Location());
        transaction.setInternalId("8a80837e75930a30017598567e4007fe");

        Location location = new Location();
        location.setAddress("300 Post St");
        location.setCity("San Francisco");
        location.setCountry("US");
        location.setLatitude(BigDecimal.valueOf(40.740352));
        location.setLongitude(BigDecimal.valueOf(-74.001761));
        location.setPostalCode("94108");
        location.setRegion("CA");
        location.setStoreNumber(1235);
        transaction.setLocation(location);

        PaymentMeta paymentMeta = new PaymentMeta();
        paymentMeta.setPayee("pete");
        paymentMeta.setPayer("cole");


        transaction.setMerchantName("United Airlines");
        transaction.setName("United Airlines");
        transaction.setPaymentChannel("in store");
        transaction.setPaymentMeta(paymentMeta);
        transaction.setPending(false);
        transaction.setPendingTransactionId(null);
        transaction.setTransactionCode(null);
        transaction.setTransactionId("VRVb7BLG1XSXv4gGZoZETMAKANggmEHWJbjkW");
        transaction.setTransactionType("special");
        transaction.setUnofficialCurrencyCode(null);
        transaction.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        transaction.setIngested(false);

        transactionRepository.save(transaction);


        return transactionRepository.findByTransactionId("VRVb7BLG1XSXv4gGZoZETMAKANggmEHWJbjkW").orElseThrow(()-> new BadRequestException("transaction not saved properly"));

    }
}
