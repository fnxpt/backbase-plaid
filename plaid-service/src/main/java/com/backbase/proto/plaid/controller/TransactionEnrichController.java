package com.backbase.proto.plaid.controller;

import com.backbase.proto.plaid.service.TransactionsService;
import com.backbase.proto.plaid.service.api.EnrichApi;
import com.backbase.proto.plaid.service.model.EnrichmentResult;
import com.backbase.proto.plaid.service.model.Transaction;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class sets up and builds a Plaid Webhook, this webhook notifies DBS when data is available for retrieval.
 */
@Slf4j
@RestController
@RequiredArgsConstructor

public class TransactionEnrichController implements EnrichApi {

    private final TransactionsService transactionService;

    @Override
    public ResponseEntity<List<EnrichmentResult>> enrichTransactions(@Valid List<Transaction> transaction) {
        log.info("Enriching transactions: {}", transaction.stream().map(Transaction::getId).collect(Collectors.joining(",")));
        return ResponseEntity.ok(transactionService.enrichTransactions(transaction));
    }
}
