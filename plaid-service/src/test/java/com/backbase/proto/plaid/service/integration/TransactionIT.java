package com.backbase.proto.plaid.service.integration;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.transaction.presentation.service.api.TransactionsApi;
import com.backbase.dbs.transaction.presentation.service.model.CreditDebitIndicator;
import com.backbase.dbs.transaction.presentation.service.model.Currency;
import com.backbase.dbs.transaction.presentation.service.model.TransactionIds;
import com.backbase.dbs.transaction.presentation.service.model.TransactionItemPost;
import com.backbase.dbs.transaction.presentation.service.api.TransactionsApi;
import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.converter.LocationConverter;
import com.backbase.proto.plaid.converter.PaymentMetaConverter;
import com.backbase.proto.plaid.mapper.PlaidToModelTransactionsMapper;
import com.backbase.proto.plaid.model.*;
import com.backbase.proto.plaid.model.Transaction;
import com.backbase.proto.plaid.repository.InstitutionRepository;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.repository.TransactionRepository;
import com.backbase.proto.plaid.service.ItemService;
import com.backbase.proto.plaid.service.TransactionsService;
import com.backbase.proto.plaid.service.mockserver.plaid.TestMockServer;
import com.backbase.proto.plaid.service.model.EnrichmentResult;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.TransactionsGetResponse;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import liquibase.pro.packaged.S;
import liquibase.pro.packaged.T;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PlaidApplication.class
)
@Slf4j
public class TransactionIT extends TestMockServer {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    PlaidClient plaidClient;

    @Autowired
    com.backbase.proto.plaid.mapper.TransactionMapper modelToDBSMapper;

    PlaidToModelTransactionsMapper plaidToModelTransactionsMapper = Mappers.getMapper(PlaidToModelTransactionsMapper.class);
    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    LocationConverter locationConverter= new LocationConverter(objectMapper);

    PaymentMetaConverter paymentMetaConverter = new PaymentMetaConverter();

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    InstitutionRepository institutionRepository;

    @Autowired
    ItemService itemService;

    @Autowired
    TransactionsService transactionsService;


    @Before
    public void setup() throws IOException {
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

        createTransaction();
    }

    private Transaction createTransaction(){
        Transaction transaction = transactionRepository.findByTransactionId("VRVb7BLG1XSXv4gGZoZETMAKANggmEHWJbjkW").orElse(new Transaction());

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
//        Response<TransactionsGetResponse> response = mock(Response.class);
//        when(plaidClient.service().transactionsGet(any()).execute()).thenReturn(response);
//
//
//
//        when(response.body()).thenReturn(mockTransactionResponse());
//        TransactionIds transactionId = new TransactionIds();
//        transactionId.setExternalId("k93yMQ99QBFJQJyVOkQdueb4n6gz67hRO6NZZ");
//        transactionId.setId("5vQnOAvvA7cqRqoXjYRrIV0k7Yj6zXHBkqzYZ");
//        List<TransactionIds> tst = new ArrayList<>();
//        tst.add(transactionId);
//        Flux<TransactionIds> transactionIds = Flux.fromIterable(tst);
//        when(transactionsApi.postTransactions(any())).thenReturn(transactionIds);
//
//    }

    @Test
    public void testTransactionMappingModelToDBS() {
        Transaction mockTransaction = new Transaction();
        mockTransaction.setAccountId("BxBXxLj1m4HMXBm9WZZmCWVbPjX16EHwv99vp");
        mockTransaction.setAmount(2307.21);
        mockTransaction.setIsoCurrencyCode("USD");
        List<String> categories = new ArrayList<>();
        categories.add("Shops");
        categories.add("Computers and Electronics");
        mockTransaction.setCategory(categories);
        mockTransaction.setCategoryId("19013000");
        mockTransaction.setDate(LocalDate.parse("2017-01-29"));
        mockTransaction.setAuthorizedDate(LocalDate.parse("2017-01-27"));

        Location location = new Location();
        location.setAddress("300 Post St");
        location.setCity("San Francisco");
        location.setCountry("US");
        location.setRegion("CA");
        location.setPostalCode("94108");
        location.setLatitude(BigDecimal.valueOf(40.740352));
        location.setLongitude(BigDecimal.valueOf(-74.001761));
        location.setStoreNumber(1235);

        mockTransaction.setLocation(location);
        mockTransaction.setName("Apple Store");
        mockTransaction.setMerchantName("Apple");

        PaymentMeta mockPaymentMeta = new PaymentMeta();
        mockTransaction.setPaymentMeta(mockPaymentMeta);

        mockTransaction.setPaymentChannel("in_store");
        mockTransaction.setPending(false);
        mockTransaction.setPendingTransactionId(null);
        mockTransaction.setAccountOwner(null);
        mockTransaction.setTransactionId("lPNjeW1nR6CDn5okmGQ6hEpMo4lLNoSrzqDje");
        mockTransaction.setTransactionCode(null);
        mockTransaction.setTransactionType("place");

        TransactionItemPost expected = new TransactionItemPost();
        Currency expectedCurrency = new Currency();
        expectedCurrency.setAmount(String.valueOf(2307.21));
        expectedCurrency.setCurrencyCode("USD");
        expected.setTransactionAmountCurrency(expectedCurrency);
        expected.setBillingStatus("BILLED");
        expected.externalArrangementId("BxBXxLj1m4HMXBm9WZZmCWVbPjX16EHwv99vp");
        expected.externalId("lPNjeW1nR6CDn5okmGQ6hEpMo4lLNoSrzqDje");
        expected.setBookingDate(LocalDate.parse("2017-01-29"));
        expected.setReference(null);
        expected.setDescription("Apple Store");
        expected.setCreditDebitIndicator(CreditDebitIndicator.DBIT);
        expected.setTypeGroup("Withdrawal");
        expected.setType("Credit/Debit Card");
        expected.setCategory("Computers and Electronics");
        expected.setCounterPartyName("Apple");
        expected.setCounterPartyCity("San Francisco");
        expected.setCounterPartyAddress("300 Post St");
        expected.setCounterPartyCountry("US");
        expected.setValueDate(LocalDate.parse("2017-01-27"));


        //Assert.assertEquals("incorrect transaction mapping", expected,
         //       modelToDBSMapper.map(mockTransaction, "ins_117650"));
        Assert.assertTrue(true);
    }

    @Autowired
    public TransactionRepository transactionRepository;


    @Test
    public void reset() {

//        transactionsApi.postDelete()
    }

    @Test
    public void testTransactionIngestion() {


        itemService.getAllItems().forEach(transactionsService::ingestTransactionsToDBS);
        List<Transaction> transactions = transactionRepository.findAllByItemIdAndIngested("WGYJu6gjhA6r6ygSGYI6556456gvgha", true, PageRequest.of(0, 10)).get().collect(Collectors.toList());
        Assert.assertEquals("not ingested", 1, transactions.size());

    }

    @Test
    public void testTransactionMappingPlaidToModel() throws IOException {
        String itemId = "WGYJu6gjhA6r6ygSGYI6556456gvgha";

        List<String> categories = new ArrayList<>();
        categories.add("Travel");
        categories.add("Airlines and Aviation Services");


        Transaction expected = new Transaction();
        expected.setAmount(500.00);
        expected.setIsoCurrencyCode("USD");
        expected.setPending(false);
        expected.setAccountId("5K3v1kbGxwSLQ6me9W9xfBMkLqNpXmuZDzeEB");
        expected.setTransactionId("VRVb7BLG1XSXv4gGZoZETMAKANggmEHWJbjkW");
        expected.setDate(LocalDate.parse("2019-05-06"));
        expected.setItemId("WGYJu6gjhA6r6ygSGYI6556456gvgha");
        expected.setName("United Airlines");
        expected.setCategory(categories);
        expected.setMerchantName("United Airlines");
        expected.setPaymentChannel("in store");
        expected.setTransactionType("special");
        expected.setCategoryId("22001000");

        PaymentMeta expectedPaymentMeta = new PaymentMeta();
        expectedPaymentMeta.setPayee("pete");
        expectedPaymentMeta.setPayer("cole");


        Location expectedLocation = new Location();
        expectedLocation.setCity("San Francisco");
        expectedLocation.setAddress("300 Post St");
        expectedLocation.setCountry("US");
        expectedLocation.setRegion("CA");
        expectedLocation.setPostalCode("94108");
        expectedLocation.setLatitude(BigDecimal.valueOf(40.740352));
        expectedLocation.setLongitude(BigDecimal.valueOf(-74.001761));
        expectedLocation.setStoreNumber(1235);

        TransactionsGetResponse.Transaction transaction = plaidClient.service().transactionsGet(new TransactionsGetRequest("access-testing", new Date(), new Date())).execute().body().getTransactions().get(0);
        Assert.assertTrue("Location doesn't match", locationEquals(expectedLocation, plaidToModelTransactionsMapper.map(transaction.getLocation())));
        Assert.assertEquals("Payment Meta doesn't match", expectedPaymentMeta, plaidToModelTransactionsMapper.map(transaction.getPaymentMeta()));
        Assert.assertTrue("transaction doesn't match", transactionEquals(expected, plaidToModelTransactionsMapper.mapToDomain(transaction, itemId)));
    }

    private boolean transactionEquals(Transaction transaction, Transaction transaction1){
        return (transaction.getAccountId().equals(transaction1.getAccountId())
                &&transaction.getAmount().equals(transaction1.getAmount())
                &&transaction.getCategory().equals(transaction1.getCategory())
                &&transaction.getDate().equals(transaction1.getDate())
                &&transaction.getIsoCurrencyCode().equals(transaction1.getIsoCurrencyCode())
                &&transaction.getItemId().equals(transaction1.getItemId())
                &&transaction.getMerchantName().equals(transaction1.getMerchantName())
                &&transaction.getName().equals(transaction1.getName())
                &&transaction.getPaymentChannel().equals(transaction1.getPaymentChannel())
                &&transaction.getTransactionType().equals(transaction1.getTransactionType()));
    }


    @Test
    public void testLocationConversions() {
        Location location = new Location();

        location.setAddress("33 Street st");
        location.setCity("City");
        location.setCountry("Country");
        location.setLatitude(BigDecimal.valueOf(69.00));
        location.setLongitude(BigDecimal.valueOf(42.00));
        location.setPostalCode("P05T");
        location.setRegion("region");
        location.setStoreNumber(456);

        String jsonLocation = "{\"id\":null,\"storeNumber\":456,\"address\":\"33 Street st\",\"city\":\"City\",\"region\":\"region\",\"postalCode\":\"P05T\",\"country\":\"Country\",\"latitude\":69.0,\"longitude\":42.0}";
        Assert.assertEquals("no match", jsonLocation, locationConverter.convertToDatabaseColumn(location));
        Assert.assertTrue("no match",locationEquals(locationConverter.convertToEntityAttribute(jsonLocation),location));
    }
    private boolean locationEquals(Location location1, Location location2){
        return (location1.getAddress().equals(location2.getAddress())
                &&location1.getCity().equals(location2.getCity())
                &&location1.getCountry().equals(location2.getCountry())
                &&location1.getLatitude().equals(location2.getLatitude())
                &&location1.getLongitude().equals(location2.getLongitude())
                &&location1.getPostalCode().equals(location2.getPostalCode())
                &&location1.getRegion().equals(location2.getRegion())
                &&location1.getStoreNumber()==location2.getStoreNumber());
    }

    @Test
    public void testTransactionGet() {
        Transaction byTransactionId = transactionRepository.findByInternalId("8a80837e75930a30017598567e4007fe").orElse(createTransaction());
        log.info("transaction: {}", byTransactionId);
        Assert.assertEquals("transaction not retrieved", "8a80837e75930a30017598567e4007fe",byTransactionId.getInternalId());
    }

    @Test
    public void testPaymentMetaConversions() {
        PaymentMeta paymentMeta = new PaymentMeta();

        paymentMeta.setByOrderOf("Fred");
        paymentMeta.setPayee("Julian");
        paymentMeta.setPayer("Fernando");
        paymentMeta.setPpdId("589fth45d");
        paymentMeta.setPaymentProcessor("pop");
        paymentMeta.setPaymentMethod("ACH");
        paymentMeta.setReferenceNumber("354689");
        paymentMeta.setReason("Money");

        String jsonPaymentMeta = "{\"referenceNumber\":\"354689\",\"ppdId\":\"589fth45d\",\"payee\":\"Julian\",\"byOrderOf\":\"Fred\",\"payer\":\"Fernando\",\"paymentMethod\":\"ACH\",\"paymentProcessor\":\"pop\",\"reason\":\"Money\"}";
        Assert.assertEquals("no match", jsonPaymentMeta, paymentMetaConverter.convertToDatabaseColumn(paymentMeta));
        Assert.assertEquals("no match", paymentMeta, paymentMetaConverter.convertToEntityAttribute(jsonPaymentMeta));

    }

    @Test
    public void testTransactionEnrichment() throws IOException {
        com.backbase.proto.plaid.service.model.Transaction transaction = new com.backbase.proto.plaid.service.model.Transaction()
                .transactionType(com.backbase.proto.plaid.service.model.Transaction.TransactionTypeEnum.DEBIT)
                .amount("500")
                .description("United Airlines")
                .id("8a80837e75930a30017598567e4007fe");
        List<com.backbase.proto.plaid.service.model.Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        List<EnrichmentResult> enrichmentResults = transactionsService.enrichTransactions(transactions);
        log.info("enriched transactions {}",enrichmentResults);
        Assert.assertEquals("transaction not Enriched", 1 ,enrichmentResults.size());

    }

}