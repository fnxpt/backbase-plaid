package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.service.ItemService;
import com.backbase.proto.plaid.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PlaidApplication.class
)
@Slf4j
public class TransactionIT {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired public ItemService itemService;
    @Autowired public TransactionsService transactionsService;

    @Test
    public void testTransactionIngestion() {
        itemService.getAllItems().forEach(transactionsService::ingestTransactionsToDBS);
    }



//    @Autowired
//    com.backbase.proto.plaid.mapper.PlaidToModelTransactionsMapper plaidToModelTransactionsMapper;

//    @Test
//    public void testTransactionMapping(){
//        TransactionsGetResponse.Transaction mockTransaction = mock(TransactionsGetResponse.Transaction.class);
//
//        when(mockTransaction.getAccountId()).thenReturn("BxBXxLj1m4HMXBm9WZZmCWVbPjX16EHwv99vp");
//        when(mockTransaction.getAmount()).thenReturn(2307.21);
//        when(mockTransaction.getIsoCurrencyCode()).thenReturn("USD");
//        List<String> categories = new ArrayList<>();
//        categories.add("Shops");
//        categories.add("Computers and Electronics");
//        when(mockTransaction.getCategory()).thenReturn(categories);
//        when(mockTransaction.getCategoryId()).thenReturn("19013000");
//        when(mockTransaction.getDate()).thenReturn("2017-01-29");
//        when(mockTransaction.getAuthorizedDate()).thenReturn("2017-01-27");
//
//        TransactionsGetResponse.Transaction.Location location = mock(TransactionsGetResponse.Transaction.Location.class);
//        when(location.getAddress()).thenReturn("300 Post St");
//        when(location.getCity()).thenReturn("San Francisco");
//        when(location.getCountry()).thenReturn("US");
//        when(location.getRegion()).thenReturn("CA");
//        when(location.getPostalCode()).thenReturn("94108");
//        when(location.getLat()).thenReturn(40.740352);
//        when(location.getLon()).thenReturn(-74.001761);
//        when(location.getStoreNumber()).thenReturn("1235");
//
//        when(mockTransaction.getLocation()).thenReturn(location);
//        when(mockTransaction.getName()).thenReturn("Apple Store");
//        when(mockTransaction.getMerchantName()).thenReturn("Apple");
//
//        TransactionsGetResponse.Transaction.PaymentMeta mockPaymentMeta = mock(TransactionsGetResponse.Transaction.PaymentMeta.class);
//        when(mockTransaction.getPaymentMeta()).thenReturn(mockPaymentMeta);
//
//        when(mockTransaction.getPaymentChannel()).thenReturn("in_store");
//        when(mockTransaction.getPending()).thenReturn(false);
//        when(mockTransaction.getPendingTransactionId()).thenReturn(null);
//        when(mockTransaction.getAccountOwner()).thenReturn(null);
//        when(mockTransaction.getTransactionId()).thenReturn("lPNjeW1nR6CDn5okmGQ6hEpMo4lLNoSrzqDje");
//        when(mockTransaction.getTransactionCode()).thenReturn(null);
//        when(mockTransaction.getTransactionType()).thenReturn("place");
//
//        TransactionItemPost expected = new TransactionItemPost();
//        expected.setBillingStatus("BILLED");
//        expected.externalArrangementId("BxBXxLj1m4HMXBm9WZZmCWVbPjX16EHwv99vp");
//        expected.externalId("lPNjeW1nR6CDn5okmGQ6hEpMo4lLNoSrzqDje");
//        expected.setBookingDate(LocalDate.parse("2017-01-29"));
//        expected.setReference(null);
//        expected.setDescription("Apple Store");
//
//
//        Assert.assertEquals("incorrect transcation mapping",expected,
//                plaidToModelTransactionsMapper.map(mockTransaction, "ins_117650"));
//    }



}
