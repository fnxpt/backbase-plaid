package com.backbase.transaction.enrichment.provider.plaid.api;

import com.backbase.proto.plaid.service.api.EnrichApi;
import com.backbase.transaction.enrichment.provider.api.TransactionEnrichmentLoader;
import com.backbase.transaction.enrichment.provider.domain.EnrichmentResult;
import com.backbase.transaction.enrichment.provider.domain.Location;
import com.backbase.transaction.enrichment.provider.domain.Merchant;
import com.backbase.transaction.enrichment.provider.domain.Transaction;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;

/**
 * to be moved
 * <p>
 * enriches transactions with extra data
 */
@RequiredArgsConstructor
@Slf4j
public class PlaidTransactionEnrichmentLoader implements TransactionEnrichmentLoader {

    private final EnrichApi enrichApi;

    /**
     * gets the name of the source of enriching data
     *
     * @return plaid
     */
    public String getName() {
        return "plaid";
    }

    /**
     * enriches a transaction with categories from plaid
     *
     * @param transactions transactions to be enriched
     * @return enriched transactions
     */
    public List<EnrichmentResult> enrichTransactions(List<Transaction> transactions) {
        log.info("Enriching {} plaid transactions", transactions.size());
        List<com.backbase.proto.plaid.service.model.Transaction> transaction = mapTransactions(transactions);

        List<com.backbase.proto.plaid.service.model.EnrichmentResult> enrichmentResults = enrichApi.enrichTransactions(transaction);

        return mapEnrichmentResults(enrichmentResults);
    }

    List<com.backbase.proto.plaid.service.model.Transaction> mapTransactions(List<com.backbase.transaction.enrichment.provider.domain.Transaction> transactions) {
        return transactions.stream().map(this::mapTransaction)
            .collect(Collectors.toList());
    }

    private com.backbase.proto.plaid.service.model.Transaction mapTransaction(com.backbase.transaction.enrichment.provider.domain.Transaction source) {
        return new com.backbase.proto.plaid.service.model.Transaction()
            .id(source.getId())
            .amount(source.getAmount().toString())
            .transactionType(com.backbase.proto.plaid.service.model.Transaction.TransactionTypeEnum.fromValue(source.getType().toString()))
            .description(source.getDescription());
    }

    List<EnrichmentResult> mapEnrichmentResults(List<com.backbase.proto.plaid.service.model.EnrichmentResult> enrichmentResult) {
        return enrichmentResult.stream().map(this::mapEnrichmentResult).collect(Collectors.toList());
    }

    private EnrichmentResult mapEnrichmentResult(com.backbase.proto.plaid.service.model.EnrichmentResult source) {
        return EnrichmentResult.builder()
            .id(Objects.requireNonNull(source.getId()))
            .categoryId(Objects.requireNonNull(source.getCategoryId()))
            .description(Objects.requireNonNull(source.getDescription()))
            .merchant(mapMerchant(source.getMerchant()))
            .build();

    }

    private Optional<? extends Merchant> mapMerchant(com.backbase.proto.plaid.service.model.Merchant source) {
        if(source == null) {
            return Optional.empty();
        }
        return Optional.of(Merchant.builder()
            .id(Objects.requireNonNull(source.getId()))
            .logo(Optional.ofNullable(source.getLogo()))
            .location(mapLocation(source.getLocation()))
            .website(Optional.ofNullable(source.getWebsite()))
            .build());

    }

    private Optional<? extends Location> mapLocation(com.backbase.proto.plaid.service.model.Location source) {
        if(source == null)  {
            return Optional.empty();
        }
        return Optional.of(Location.builder()
            .id(Objects.requireNonNull(source.getId()))
            .latitude(Optional.ofNullable(source.getLatitude()))
            .longitude(Optional.ofNullable(source.getLongitude()))
            .addressLine1(Optional.ofNullable(source.getAddressLine1()))
            .addressLine2(Optional.ofNullable(source.getAddressLine2()))
            .city(Optional.ofNullable(source.getCity()))
            .state(Optional.ofNullable(source.getState()))
            .postCode(Optional.ofNullable(source.getPostCode()))
            .country(Optional.ofNullable(source.getCountry()))
            .completeAddress(Optional.ofNullable(source.getCompleteAddress()))
            .build());
    }





}
