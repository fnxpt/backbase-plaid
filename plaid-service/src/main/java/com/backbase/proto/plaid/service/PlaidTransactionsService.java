package com.backbase.proto.plaid.service;

import com.backbase.dbs.transaction.presentation.service.model.TransactionItemPost;
import com.backbase.dbs.transaction.presentation.service.model.TransactionsDeleteRequestBody;
import com.backbase.proto.plaid.mapper.TransactionMapper;
import com.backbase.stream.TransactionService;
import com.backbase.stream.configuration.AccessControlConfiguration;
import com.backbase.stream.configuration.TransactionServiceConfiguration;
import com.backbase.stream.product.ProductIngestionSagaConfiguration;
import com.backbase.stream.productcatalog.configuration.ProductCatalogServiceConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.ErrorResponse;
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

    private final ItemService itemService;
    static int testCount = 0;

    private final ObjectMapper objectMapper;

    public void ingestInitialUpdate(String itemId) {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        // Pull transactions for a date range
        this.ingestTransactions(itemId, startDate, endDate);
    }

    public void ingestHistoricalUpdate(String itemId) {
        LocalDate startDate = LocalDate.now().minusYears(2);
        LocalDate endDate = LocalDate.now();
        // Pull transactions for a date range
        this.ingestTransactions(itemId, startDate, endDate);
    }

    public void ingestDefaultUpdate(String itemId) {
        LocalDate startDate = LocalDate.now().minusDays(14);
        LocalDate endDate = LocalDate.now();
        // Pull transactions for a date range
        this.ingestTransactions(itemId, startDate, endDate);

    }

    public void removeTransactions(String itemId, List<String> removedTransactions) {
        List<TransactionsDeleteRequestBody> deleteRequests = removedTransactions.stream().map(id -> new TransactionsDeleteRequestBody().id(id)).collect(Collectors.toList());
        transactionService.deleteTransactions(Flux.fromIterable(deleteRequests));
    }

    @SneakyThrows
    public void ingestTransactions(String itemId, LocalDate startDate, LocalDate endDate) {
        String accessToken = itemService.getAccessToken(itemId);
        this.ingestTransactions(accessToken, startDate, endDate, 100, 0);
    }

    @SneakyThrows
    private void ingestTransactions(String accessToken, LocalDate startDate, LocalDate endDate, int batchSize, int offset) {
        log.info("Ingesting transactions from: startDate: {} to: {} with batchSize: {} from offset: {}", startDate, endDate, batchSize, offset);
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

        if (response.isSuccessful()) {
            //POST https://host:port/transaction-manager/service-api/v2/transactions
            TransactionsGetResponse transactionsGetResponse = response.body();
            log.info("response: {}", transactionsGetResponse);


            transactionsGetResponse.getItem().getInstitutionId();

            //convert transactions from plaid into transactions dbs

            // everytime called its set to null no the total tranactions in the list will always be less then all the transaction wanted

            List<TransactionItemPost> transactionItemPosts = null;
            if (transactionsGetResponse != null) {
                // populates list with response
                List<TransactionsGetResponse.Transaction> transactions = transactionsGetResponse.getTransactions();

//            objectMapper.writeValue(new File("plaid-" + LocalDateTime.now().toString() +".json"), transactions);

                transactionItemPosts = transactions.stream().map((TransactionsGetResponse.Transaction transaction) ->
                    transactionMapper.map(transaction, transactionsGetResponse.getItem().getInstitutionId())).collect(Collectors.toList());

                Integer totalTransactionsRequested = transactionsGetResponse.getTotalTransactions();
                int totalTransactionRetrieved = transactionItemPosts.size();
                // if there are too many left to retrieve get the next batch
                int newOffset = offset + totalTransactionRetrieved;
                log.info("number of items retrieved: {}", newOffset);
                log.info("number of items expected: {}", totalTransactionsRequested);
                log.info("number of times looped: {}", testCount++);
                log.info("number retrieved this time : {}", totalTransactionRetrieved);

                //processes list to Backbase
                transactionService.processTransactions(Flux.fromIterable(transactionItemPosts))
                    .doOnNext(transactionIds -> log.info("Ingested transactionIds: {}", transactionIds))
                    .collectList()
                    .block();

                if (batchSize < (totalTransactionsRequested - offset)) {
                    log.info("Ingesting next page of transactions from: {}", newOffset);
                    ingestTransactions(accessToken, startDate, endDate, batchSize, newOffset);
                } else {
                    log.info("Finiished ingestion of transactions");
                }

            }
        } else {
            ErrorResponse errorResponse = plaidClient.parseError(response);
            log.error("Failed to ingest transactions for: {}", errorResponse.getErrorMessage());
        }

    }

    public Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant());
    }


}
