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
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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


    @SneakyThrows
    public void ingestTransactions(String accessToken) {
        SimpleDateFormat simpleDateFormat = new
                SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        startDate = simpleDateFormat.parse("2018-01-01");

        Date endDate = new Date();
        // Pull transactions for a date range
        Response<TransactionsGetResponse> response =
                plaidClient.service().transactionsGet(
                        new TransactionsGetRequest(
                                accessToken,
                                startDate,
                                endDate))
                        .execute();
        //POST https://host:port/transaction-manager/service-api/v2/transactions
        TransactionsGetResponse body = response.body();
        log.info("response: {}", body);
        // transactionService.processTransactions(response.body().getTransactions());
        //convert transactions from plaid into transactions dbs

        List<TransactionItemPost> transactionItemPosts = null;
        if (body != null) {
            transactionItemPosts = body.getTransactions().stream().map(transaction -> transactionMapper.map(transaction)).collect(Collectors.toList());
            transactionService.processTransactions(Flux.fromIterable(transactionItemPosts))
                    .doOnNext(transactionIds -> log.info("Ingested transactionIds: {}", transactionIds))
                    .collectList()
                    .block();
        }

    }


}
