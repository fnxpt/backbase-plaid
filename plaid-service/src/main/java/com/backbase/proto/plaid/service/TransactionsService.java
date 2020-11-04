package com.backbase.proto.plaid.service;

import com.backbase.dbs.transaction.presentation.service.api.TransactionsApi;
import com.backbase.dbs.transaction.presentation.service.model.TransactionIds;
import com.backbase.dbs.transaction.presentation.service.model.TransactionItemPost;
import com.backbase.dbs.transaction.presentation.service.model.TransactionsDeleteRequestBody;
import com.backbase.proto.plaid.exceptions.IngestionFailedException;
import com.backbase.proto.plaid.mapper.TransactionMapper;
import com.backbase.proto.plaid.mapper.PlaidToModelTransactionsMapper;
import com.backbase.proto.plaid.model.Item;
import com.backbase.proto.plaid.model.Transaction;
import com.backbase.proto.plaid.repository.TransactionRepository;
import com.backbase.proto.plaid.service.model.EnrichmentResult;
import com.backbase.stream.configuration.AccessControlConfiguration;
import com.backbase.stream.configuration.TransactionServiceConfiguration;
import com.backbase.stream.product.ProductIngestionSagaConfiguration;
import com.backbase.stream.productcatalog.configuration.ProductCatalogServiceConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.ErrorResponse;
import com.plaid.client.response.TransactionsGetResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
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
    private final TransactionsApi transactionsApi;
    private final PlaidToModelTransactionsMapper plaidToModelTransactionsMapper = Mappers.getMapper(PlaidToModelTransactionsMapper.class);
    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final AccessTokenService accessTokenService;

    private final ObjectMapper objectMapper;

    private ErrorHandler errorHandler;

    /**
     * Ingests the Transactions of an Item.
     *
     * @param item identifies item the transaction belong to
     */
    public void ingestInitialUpdate(Item item) throws IngestionFailedException {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        this.ingestTransactions(item, startDate, endDate);
    }

    public void ingestHistoricalUpdate(Item item) throws IngestionFailedException {
        LocalDate startDate = LocalDate.now().minusYears(2);
        LocalDate endDate = LocalDate.now();
        this.ingestTransactions(item, startDate, endDate);
    }

    public void ingestDefaultUpdate(Item item) throws IngestionFailedException {
        LocalDate startDate = LocalDate.now().minusDays(14);
        LocalDate endDate = LocalDate.now();
        this.ingestTransactions(item, startDate, endDate);
    }

    public void removeTransactions(Item item, List<String> removedTransactions) {
        List<TransactionsDeleteRequestBody> deleteRequests = removedTransactions.stream().map(id -> new TransactionsDeleteRequestBody().id(id)).collect(Collectors.toList());
        transactionsApi.postDelete(deleteRequests).block();
    }

    /**
     * Ingests Transactions, setting the start and end dates of the Transactions that are being requested
     * it also sets how many are to be ingested at one time and the offset for pagination.
     *
     * @param item      identifies the Item the Transaction belongs to
     * @param startDate the earliest Transaction date being requested
     * @param endDate   the latest Transaction being requested
     */
    public void ingestTransactions(Item item, LocalDate startDate, LocalDate endDate) throws IngestionFailedException {
        String accessToken = accessTokenService.getAccessToken(item.getItemId());
        this.storePlaidTransactions(item, accessToken, startDate, endDate, 100, 0);
        this.ingestTransactionsToDBS(item);

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
    private void storePlaidTransactions(Item item, String accessToken, LocalDate startDate, LocalDate endDate, int batchSize, int offset) throws IngestionFailedException {
        log.info("Ingesting transactions from: startDate: {} to: {} with batchSize: {} from offset: {}", startDate, endDate, batchSize, offset);
        TransactionsGetRequest transactionsGetRequest = new TransactionsGetRequest(
            accessToken,
            convertToDateViaInstant(startDate),
            convertToDateViaInstant(endDate)).withOffset(offset);
        TransactionsGetRequest.Options options = new TransactionsGetRequest.Options();
        options.count = batchSize;
        options.offset = offset;

        Response<TransactionsGetResponse> response;
        try {
            response = plaidClient.service().transactionsGet(
                transactionsGetRequest)
                .execute();
        } catch (IOException e) {
            throw new IngestionFailedException("Failed to call Plaid Client", e);
        }

        if (response.isSuccessful() && response.body() != null) {
            TransactionsGetResponse transactionsGetResponse = response.body();
            log.info("Received {} transactions from Plaid", transactionsGetResponse.getTransactions().size());

            // populates list with response
            List<TransactionsGetResponse.Transaction> transactions = transactionsGetResponse.getTransactions();

            transactions.forEach(transaction -> {
                if (!transactionRepository.existsByTransactionId(transaction.getTransactionId())) {
                    log.info("Storing transaction: {}", transaction.getTransactionId());
                    transactionRepository.save(plaidToModelTransactionsMapper.mapToDomain(transaction, item.getItemId()));
                } else {
                    log.info("Transaction: {} already stored", transaction.getTransactionId());
                }
            });

            Integer totalTransactionsRequested = transactionsGetResponse.getTotalTransactions();
            int totalTransactionRetrieved = transactions.size();
            // if there are too many left to retrieve get the next batch
            int newOffset = offset + totalTransactionRetrieved;
            log.info("number of items retrieved: {}", newOffset);
            log.info("number of items expected: {}", totalTransactionsRequested);
            log.info("number retrieved this time : {}", totalTransactionRetrieved);

            if (batchSize < (totalTransactionsRequested - offset)) {
                log.info("Ingesting next page of transactions from: {}", newOffset);
                storePlaidTransactions(item, accessToken, startDate, endDate, batchSize, newOffset);
            } else {
                log.info("Finished ingestion of transactions");
            }

        } else {
            ErrorResponse errorResponse = plaidClient.parseError(response);
            errorHandler.handleErrorResponse(errorResponse, item);
            log.error("Failed to ingest transactions for: {}. Message: {}", item.getItemId(), errorResponse.getErrorMessage());
            throw new IngestionFailedException(errorResponse);
        }
    }


    public void ingestTransactionsToDBS(Item item) {
        int pageSize = 5;
        Page<Transaction> page = transactionRepository.findAllByItemIdAndIngested(item.getItemId(), true, PageRequest.of(0, pageSize));

        List<TransactionIds> transactionIds = new ArrayList<>();

        // Ingest First Page
        page = ingestTransactionsPage(page, item, transactionIds);

        // Ingest Subsequent pages
//        while (page.hasNext()) {
//            page = ingestTransactionsPage(page, item, transactionIds);
//        }

        updateIngestedStatus(transactionIds);
    }

    @SneakyThrows
    protected Page<Transaction> ingestTransactionsPage(Page<Transaction> page, Item item, List<TransactionIds> allTransactionIds) {
        log.info("Ingesting transactions: {}", page);

        List<TransactionItemPost> transactionItemPosts = page.getContent().stream()
            .map(transaction -> transactionMapper.map(transaction, item.getInstitutionId()))
            .collect(Collectors.toList());

        String contents = objectMapper.writeValueAsString(transactionItemPosts);

        List<TransactionIds> transactionIds = transactionsApi.postTransactions(transactionItemPosts)
            .collectList()
            .onErrorResume(throwable -> {
                log.error("Failed to ingest transactions: {}", contents);
                return Mono.empty();
            })
            .block();
        assert transactionIds != null;
        allTransactionIds.addAll(transactionIds);

        if (page.hasNext()) {
            return transactionRepository.findAllByItemIdAndIngested(item.getItemId(), false, page.nextPageable());
        } else {
            return page;
        }
    }


    @Transactional
    protected List<TransactionIds> updateIngestedStatus(List<TransactionIds> transactionIds) {
        log.info("Ingested transactions: {}", transactionIds.stream().map(TransactionIds::getExternalId).collect(Collectors.joining(",")));
        if (!transactionIds.isEmpty())
            transactionRepository.updateIngested(true, transactionIds.stream().map(TransactionIds::getExternalId).collect(Collectors.toList()));
        return transactionIds;
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


    public List<EnrichmentResult> enrich(List<Transaction> transactions) {

        return Collections.emptyList();
    }

    public List<EnrichmentResult> enrichTransactions(List<com.backbase.proto.plaid.service.model.Transaction> transaction) {
        return null;
    }
}
