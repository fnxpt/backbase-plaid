package com.backbase.proto.plaid.service;

import com.backbase.dbs.transaction.presentation.service.model.TransactionItemPost;
import com.backbase.dbs.transaction.presentation.service.model.TransactionsDeleteRequestBody;
import com.backbase.proto.plaid.mapper.PlaidToDBSTransactionMapper;
import com.backbase.proto.plaid.mapper.PlaidToModelTransactionsMapper;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.Transaction;
import com.backbase.proto.plaid.repository.ItemRepository;
import com.backbase.proto.plaid.repository.TransactionRepository;
import com.backbase.stream.TransactionService;
import com.backbase.stream.configuration.AccessControlConfiguration;
import com.backbase.stream.configuration.TransactionServiceConfiguration;
import com.backbase.stream.product.ProductIngestionSagaConfiguration;
import com.backbase.stream.productcatalog.configuration.ProductCatalogServiceConfiguration;
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
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import retrofit2.Response;

/**
 * This class allows the retrieval and ingestion of Transaction data when it is available from Plaid.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Import({
    ProductIngestionSagaConfiguration.class,
    AccessControlConfiguration.class,
    ProductCatalogServiceConfiguration.class,
    TransactionServiceConfiguration.class
})
public class TransactionsService {

    private final PlaidClient plaidClient;
    private final TransactionService streamTransactionService;
    private final PlaidToDBSTransactionMapper plaidToDBSTransactionMapper;
    private final PlaidToModelTransactionsMapper plaidToModelTransactionsMapper = Mappers.getMapper(PlaidToModelTransactionsMapper.class);
    private final TransactionRepository transactionRepository;

    private final AccessTokenService accessTokenService;

    private final ItemRepository itemRepository;

    /**
     * Ingests the Transactions of an Item.
     *
     * @param item identifies item the transaction belong to
     */
    public void ingestInitialUpdate(Item item) {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        // Pull transactions for a date range
        this.ingestTransactions(item, startDate, endDate);
    }

    public void ingestHistoricalUpdate(Item item) {
        LocalDate startDate = LocalDate.now().minusYears(2);
        LocalDate endDate = LocalDate.now();
        // Pull transactions for a date range
        this.ingestTransactions(item, startDate, endDate);
    }

    public void ingestDefaultUpdate(Item item) {
        LocalDate startDate = LocalDate.now().minusDays(14);
        LocalDate endDate = LocalDate.now();
        // Pull transactions for a date range
        this.ingestTransactions(item, startDate, endDate);

    }

    public void removeTransactions(Item item, List<String> removedTransactions) {
        List<TransactionsDeleteRequestBody> deleteRequests = removedTransactions.stream().map(id -> new TransactionsDeleteRequestBody().id(id)).collect(Collectors.toList());
        streamTransactionService.deleteTransactions(Flux.fromIterable(deleteRequests));
    }

    /**
     * Ingests Transactions, setting the start and end dates of the Transactions that are being requested
     * it also sets how many are to be ingested at one time and the offset for pagination.
     *
     * @param item    identifies the Item the Transaction belongs to
     * @param startDate the earliest Transaction date being requested
     * @param endDate   the latest Transaction being requested
     */
    @SneakyThrows
    public void ingestTransactions(Item item, LocalDate startDate, LocalDate endDate) {
        String accessToken = accessTokenService.getAccessToken(item.getItemId());
        this.ingestTransactions(item, accessToken, startDate, endDate, 100, 0);
    }

    /**
     * This requests and paginates the Transactions coming in from Plaid.
     *
     * @param accessToken authentication for the Plaid request, also identifies the Item that the Transaction belong to
     * @param startDate   the earliest Transaction date being requested
     * @param endDate     the latest Transaction being requested
     * @param batchSize   the number of Transactions being ingested at one time
     * @param offset      used for pagination so each retrieval for one request is the next set of Transactions
     */
    @SneakyThrows
    private void ingestTransactions(Item item, String accessToken, LocalDate startDate, LocalDate endDate, int batchSize, int offset) {
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

        if (response.isSuccessful() && response.body() != null) {
            TransactionsGetResponse transactionsGetResponse = response.body();
            log.info("response: {}", transactionsGetResponse);

            transactionsGetResponse.getItem().getInstitutionId();

            //convert transactions from plaid into transactions dbs

            // everytime called its set to null no the total tranactions in the list will always be less then all the transaction wanted

            List<TransactionItemPost> transactionItemPosts = null;
            // populates list with response
            List<TransactionsGetResponse.Transaction> transactions = transactionsGetResponse.getTransactions();

            transactions.forEach(transaction -> {
                if (!transactionRepository.existsByTransactionId(transaction.getTransactionId()))
                    transactionRepository.save(plaidToModelTransactionsMapper.mapToDomain(transaction));
            });

            transactionItemPosts = transactions.stream().map((TransactionsGetResponse.Transaction transaction) ->
                plaidToDBSTransactionMapper.map(transaction, transactionsGetResponse.getItem().getInstitutionId())).collect(Collectors.toList());

            Integer totalTransactionsRequested = transactionsGetResponse.getTotalTransactions();
            int totalTransactionRetrieved = transactionItemPosts.size();
            // if there are too many left to retrieve get the next batch
            int newOffset = offset + totalTransactionRetrieved;
            log.info("number of items retrieved: {}", newOffset);
            log.info("number of items expected: {}", totalTransactionsRequested);
            log.info("number retrieved this time : {}", totalTransactionRetrieved);

            //processes list to Backbase
            streamTransactionService.processTransactions(Flux.fromIterable(transactionItemPosts))
                .doOnNext(transactionIds -> log.info("Ingested transactionIds: {}", transactionIds))
                .collectList()
                .block();

            if (batchSize < (totalTransactionsRequested - offset)) {
                log.info("Ingesting next page of transactions from: {}", newOffset);
                ingestTransactions(item, accessToken, startDate, endDate, batchSize, newOffset);
            } else {
                log.info("Finished ingestion of transactions");
            }

        } else {
            ErrorResponse errorResponse = plaidClient.parseError(response);
            item.setErrorCode(errorResponse.getErrorCode());
            item.setState("FAILED");
            item.setErrorDisplayMessage(errorResponse.getDisplayMessage());
            item.setErrorMessage(errorResponse.getErrorMessage());
            itemRepository.save(item);
            log.error("Failed to ingest transactions for: {}. Message: {}", item.getItemId(), errorResponse.getErrorMessage());
        }
    }

    /**
     * Converts date from local to date.
     *
     * @param dateToConvert date to be converted
     * @return converted date
     */
    public Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant());
    }

    @Transactional
    public void deleteTransactionsByAccountId(Item item, String accountId) {
        removeTransactions(item, transactionRepository.findAllByAccountId(accountId).stream()
            .map(Transaction::getTransactionId)
            .collect(Collectors.toList()));
        transactionRepository.deleteTransactionsByAccountId(accountId);
    }
}
