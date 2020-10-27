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
@AllArgsConstructor
@Slf4j
public class PlaidTransactionEnrichmentLoader implements TransactionEnrichmentLoader {

    private final PlaidCategoryLoader categoryLoader;
    private final PlaidClient plaidClient;
    @Override
    public String getName() {
        return "plaid";
    }

    @Override
    public List<EnrichmentResult> enrichTransactions(List<Transaction> transactions) {
        Map<String, Category> categories = Maps.uniqueIndex(categoryLoader.getAllCategories(false), Category::getId);
//        List<MxEnrichedTransaction> concatenatedList = splitAndCategorize(transactions)
//                .map(transaction -> withParentCategory(transaction, categories))
//                .collect(Collectors.toList());
        return null;
    }
}
