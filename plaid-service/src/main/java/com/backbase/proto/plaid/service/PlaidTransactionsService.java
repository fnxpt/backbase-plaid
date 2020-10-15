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
            convertToDateViaInstant(endDate));

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
        // transactionService.processTransactions(response.body().getTransactions());
        //convert transactions from plaid into transactions dbs

        List<TransactionItemPost> transactionItemPosts = null;
        if (body != null) {
            transactionItemPosts = body.getTransactions().stream().map(transactionMapper::map).collect(Collectors.toList());
            transactionService.processTransactions(Flux.fromIterable(transactionItemPosts))
                .doOnNext(transactionIds -> log.info("Ingested transactionIds: {}", transactionIds))
                .collectList()
                .block();
        }
        Integer totalTransactions = body.getTotalTransactions();
        // loop over incrementing index by 100 (but what am I doing in the loop ?




    }

    public Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant());
    }


}
