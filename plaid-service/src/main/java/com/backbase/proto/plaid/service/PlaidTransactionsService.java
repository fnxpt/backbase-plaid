package com.backbase.proto.plaid.service;

import com.backbase.dbs.transaction.presentation.service.model.TransactionItemPost;
import com.backbase.proto.plaid.mapper.TransactionMapper;
import com.backbase.stream.TransactionService;
import com.backbase.stream.configuration.AccessControlConfiguration;
import com.backbase.stream.configuration.TransactionServiceConfiguration;
import com.backbase.stream.product.ProductIngestionSagaConfiguration;
import com.backbase.stream.productcatalog.configuration.ProductCatalogServiceConfiguration;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.TransactionsGetResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import retrofit2.Response;

@Service
@Slf4j
@RequiredArgsConstructor
@Import({
    ProductIngestionSagaConfiguration.class,
    AccessControlConfiguration.class,
    ProductCatalogServiceConfiguration.class,
    TransactionServiceConfiguration.class
})
public class PlaidTransactionsService {

    private final PlaidClient plaidClient;
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    static int testCount= 0;


    public void ingestTransactions(String itemId) {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();


        // Pull transactions for a date range
        this.ingestTransactions(itemId, startDate, endDate);
    }

    @SneakyThrows
    public void ingestTransactions(String accessToken, LocalDate startDate, LocalDate endDate) {



        this.ingestTransactions(accessToken, startDate, endDate, 100, 0);
    }

    @SneakyThrows
    private void ingestTransactions(String accessToken, LocalDate startDate, LocalDate endDate, int batchSize, int offset) {




        TransactionsGetRequest transactionsGetRequest = new TransactionsGetRequest(
            accessToken,
            convertToDateViaInstant(startDate),
            convertToDateViaInstant(endDate)).withOffset(offset);

        TransactionsGetRequest.Options options = new TransactionsGetRequest.Options();
        options.count = batchSize;
        options.offset = offset;


        Response<TransactionsGetResponse> response =
            plaidClient.service().transactionsGet(
                transactionsGetRequest)
                .execute();
        //POST https://host:port/transaction-manager/service-api/v2/transactions
        TransactionsGetResponse body = response.body();
        log.info("response: {}", body);
        //convert transactions from plaid into transactions dbs

        // everytime called its set to null no the total tranactions in the list will always be less then all the transaction wanted

        List<TransactionItemPost> transactionItemPosts = null;
        if (body != null) {
            // populates list with response
            transactionItemPosts = body.getTransactions().stream().map(transactionMapper::map).collect(Collectors.toList());
            //processes list to Backbase
            transactionService.processTransactions(Flux.fromIterable(transactionItemPosts))
                .doOnNext(transactionIds -> log.info("Ingested transactionIds: {}", transactionIds))
                .collectList()
                .block();
        }
        Integer totalTransactionsRequested = body.getTotalTransactions();
        Integer totalTransactionRetrieved = transactionItemPosts.size();
        // if there are too many left to retrieve get the next batch
        if(batchSize < (totalTransactionsRequested-offset) ) {
            ingestTransactions(accessToken, startDate,endDate, batchSize, offset+totalTransactionRetrieved);
        }
        log.info("number of items retrieved: {}", offset+totalTransactionRetrieved);
        log.info("number of items expected: {}", totalTransactionsRequested);
        log.info("number of times looped: {}", testCount++);
        log.info("number retrieved this time : {}", totalTransactionRetrieved);

    }

    public Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant());
    }


}
