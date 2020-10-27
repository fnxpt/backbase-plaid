package com.backbase.proto.plaid.enrichment.provider;

import com.backbase.transaction.enrichment.provider.api.TransactionEnrichmentLoader;
import com.backbase.transaction.enrichment.provider.domain.Category;
import com.backbase.transaction.enrichment.provider.domain.EnrichmentResult;
import com.backbase.transaction.enrichment.provider.domain.Location;
import com.backbase.transaction.enrichment.provider.domain.Transaction;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.plaid.client.PlaidClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * to be moved
 * <p>
 * enriches transactions with extra data
 */
@AllArgsConstructor
@Slf4j
public class PlaidTransactionEnrichmentLoader implements TransactionEnrichmentLoader {

    private final PlaidCategoryLoader categoryLoader;
    private final PlaidClient plaidClient;

    /**
     * gets the name of the source of enriching data
     *
     * @returnn plaid
     */
    @Override
    public String getName() {
        return "plaid";
    }

    /**
     * enriches a transaction with categories from plaid
     *
     * @param transactions transactions to be enriched
     * @return enriched transactions
     */
    @Override
    public List<EnrichmentResult> enrichTransactions(List<Transaction> transactions) {
        Map<String, Category> categories = Maps.uniqueIndex(categoryLoader.getAllCategories(false), Category::getId);
//        List<EnrichmentResult> concatenatedList = transactions.stream().map(transaction -> getMockEntrichmentResult(transaction, categories))
////                .map(transaction -> withParentCategory(transaction, categories))
//                .collect(Collectors.toList());

//        return concatenatedList;

        return null;
    }

    /**
     * enriches individual transactions
     * @param transaction transaction to be enriched with a category
     * @param categories categories used in the enriching process
     * @return enriched transaction
     */
    private EnrichmentResult getMockEnrichmentResult(Transaction transaction, Map<String, Category> categories) {
        return null;
//        return categories.map(category -> EnrichmentResult.of(transaction.getId()));
    }
}
