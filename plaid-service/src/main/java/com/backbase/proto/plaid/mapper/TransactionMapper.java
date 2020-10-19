package com.backbase.proto.plaid.mapper;

import com.backbase.dbs.transaction.presentation.service.model.CreditDebitIndicator;
import com.backbase.dbs.transaction.presentation.service.model.Currency;
import com.backbase.dbs.transaction.presentation.service.model.TransactionItemPost;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.plaid.client.response.TransactionsGetResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * TransactionMapper:
 * Maps transactions retrieved from the plaid api end point to the backbase dbs transactions
 */
@Slf4j
@Component
public class TransactionMapper {

    private final PlaidConfigurationProperties transactionConfigurationProperties;
    private final Map<String, String> transactionTypeGroupMap;
    private final Map<String, String> transactionTypeMap;

    /**
     * Sets the configuration properties used for the mapping
     *
     * @param transactionConfigurationProperties contains methods for setting the type group and type for dbs transactions
     */
    public TransactionMapper(PlaidConfigurationProperties transactionConfigurationProperties) {
        this.transactionConfigurationProperties = transactionConfigurationProperties;
        this.transactionTypeGroupMap = transactionConfigurationProperties.getTransactions().getTransactionTypeGroupMap();
        this.transactionTypeMap = transactionConfigurationProperties.getTransactions().getTransactionTypeMap();
    }
    /**
     * This maps the individual fields of the plaid transaction to the backbase transaction
     *
     * @param transaction a transaction from the list retrieved by plaid
     * @return DBS transaction for ingestion
     */
    public TransactionItemPost map(TransactionsGetResponse.Transaction transaction, String institutionId) {
        // CreditDebitIndicator credit = new CreditDebitIndicator();

        String arrangementId = transaction.getAccountId();

        Currency transactionAmountCurrency = new Currency();

        TransactionItemPost bbTransaction = new TransactionItemPost();
        //set required data
        bbTransaction.setExternalArrangementId(arrangementId);
        bbTransaction.setExternalId(transaction.getTransactionId());
        bbTransaction.setBookingDate(LocalDate.parse(transaction.getDate()));

        mapDescription(transaction, bbTransaction, institutionId);

        BigDecimal amount = BigDecimal.valueOf(transaction.getAmount());

        CreditDebitIndicator indicator;
        boolean amountIsNegative = amount.compareTo(BigDecimal.ZERO) < 0;
        if (amountIsNegative) {
            log.info("Amount: {} is negative", amount);
            indicator = CreditDebitIndicator.CRDT;
            amount = amount.negate();
        } else {
            indicator = CreditDebitIndicator.DBIT;
        }
        transactionAmountCurrency.setAmount(String.valueOf(amount));
        transactionAmountCurrency.setCurrencyCode(transaction.getIsoCurrencyCode());
        bbTransaction.setTransactionAmountCurrency(transactionAmountCurrency);


        bbTransaction.setCreditDebitIndicator(indicator);

        String transactionTypeGroup = getTransactionTypeGroup(transaction);
        String transactionType = getTransactionType(transaction);

        bbTransaction.setTypeGroup(transactionTypeGroup);
        bbTransaction.setType(transactionType);
        // nullable data
        bbTransaction.setCategory(transaction.getCategory().get(0));
        TransactionsGetResponse.Transaction.PaymentMeta paymentMeta = transaction.getPaymentMeta();
        String referenceNumber = paymentMeta.getReferenceNumber();

        bbTransaction.setReference(referenceNumber);
        // counter party data
        mapCounterParty(transaction, bbTransaction, institutionId);
        mapCounterPartyBBAN(transaction, bbTransaction, institutionId);

        mapLocation(transaction, bbTransaction);

        String billingStatus = mapBilling(transaction);

        bbTransaction.setBillingStatus(billingStatus);
        log.info("Mapped Billing Status: {}", billingStatus);

        if (transaction.getAuthorizedDate() != null) {
            bbTransaction.setValueDate(LocalDate.parse(transaction.getAuthorizedDate()));
        }


        return bbTransaction;
    }

    /**
     * Maps the descriptions from the plaid transaction response to backbase dbs transaction
     * it uses the description parser in plaid configuration properties to do so
     *
     * @param transaction the transaction response sent rom plaid
     * @param bbTransaction the transaction to be ingested by backbase
     * @param institutionId the identifier for the institution that the transaction belongs to
     */
    private void mapDescription(TransactionsGetResponse.Transaction transaction, TransactionItemPost bbTransaction, String institutionId) {
        String description;

        PlaidConfigurationProperties.DescriptionParser descriptionParser = getDescriptionParser(institutionId);
        String transactionName = transaction.getName();
        if (descriptionParser != null) {
            description = parse(transactionName, descriptionParser.getDescription());

        } else {
            description = transactionName;
        }

        log.info("Mapped transaction name: {} to description: {}", transactionName, description);

        description = StringUtils.abbreviate(description, 140);
        bbTransaction.setDescription(description);
    }


    /**
     * Maps the location from the plaid returned transaction which stores it as an object to the backbase transaction which stores it as separate fields
     *
     * @param transaction transaction parsed from plaids clients side
     * @param bbTransaction backbase transaction to be ingested and displayed in the front end
     */
    private void mapLocation(TransactionsGetResponse.Transaction transaction, TransactionItemPost bbTransaction) {
        TransactionsGetResponse.Transaction.Location location = transaction.getLocation();

        if (location != null) {
            bbTransaction.setCounterPartyCity(location.getCity());
            bbTransaction.setCounterPartyAddress(location.getAddress());
            bbTransaction.setCounterPartyCountry(location.getCountry());
        }
    }

    /**
     * Maps counter party attributes from plaid where they are less grouped
     *
     * @param transaction the transaction response from plaid
     * @param bbTransaction the backbase transaction to be ingested
     * @param institutionId identifies the institution the transaction belongs to
     */
    private void mapCounterParty(TransactionsGetResponse.Transaction transaction, TransactionItemPost bbTransaction, String institutionId) {
        String counterpartyName;
        TransactionsGetResponse.Transaction.PaymentMeta paymentMeta = transaction.getPaymentMeta();

        if (transaction.getMerchantName() != null) {
            counterpartyName = transaction.getMerchantName();
            log.info("Mapped counter party name from merchant name: {}", counterpartyName);
        } else if (paymentMeta.getPayee() != null) {
            counterpartyName = paymentMeta.getPayee();
            log.info("Mapped counter party name from payee name: {}", counterpartyName);
        } else {
            PlaidConfigurationProperties.DescriptionParser descriptionParser = getDescriptionParser(institutionId);
            if (descriptionParser != null) {
                counterpartyName = parse(transaction.getName(), descriptionParser.getCounterPartyName());
                log.info("Mapped counter party name: {} from transaction name: {}", transaction.getName(), counterpartyName);
            } else {
                counterpartyName = transaction.getName();
            }
        }
        if (counterpartyName.length() > 128) {
            log.warn("Counter party name: {} cannot be longer than 128 characters", counterpartyName);
            counterpartyName = StringUtils.abbreviate(counterpartyName, 128);
        }
        bbTransaction.setCounterPartyName(counterpartyName);
    }

    /**
     * Maps the Counter party account number (BBAN) if it is available from plaid
     *
     * @param transaction the transaction response from plaid
     * @param bbTransaction the backbase transaction to be ingested
     * @param institutionId identifies the institution the transaction belongs to
     */
    private void mapCounterPartyBBAN(TransactionsGetResponse.Transaction transaction, TransactionItemPost bbTransaction, String institutionId) {
        PlaidConfigurationProperties.DescriptionParser descriptionParser = getDescriptionParser(institutionId);
        if (descriptionParser != null) {
            getMatch(transaction.getName(), descriptionParser.getCounterPartyBBAN())
                .ifPresent(counterPartyAccountNumber -> {
                    log.info("Mapping counter account number: {}", counterPartyAccountNumber);
                    bbTransaction.setCounterPartyAccountNumber(StringUtils.abbreviate(counterPartyAccountNumber, 36));
                });
        }

    }

    /**
     * Maps billing status from Plaid transaction to backbase transaction
     *
     * @param transaction the transaction response from plaid
     * @return the billing status to be added to the backbase transaction
     */
    @NotNull
    private String mapBilling(TransactionsGetResponse.Transaction transaction) {
        String billingStatus;

        if (transaction.getPending())
            billingStatus = "PENDING";
        else
            billingStatus = "BILLED";
        return billingStatus;
    }

    /**
     * Formats a text description to be mapped to a transaction
     *
     * @param text to be formatted
     * @param regexPatterns desired format pattern in regular expression
     * @return formatted description
     */
    private String parse(String text, List<String> regexPatterns) {
        String description;
        description = getMatch(text, regexPatterns)
            .orElse(text);
        return description;
    }

    /**
     * Formats a string using regular expression
     *
     * @param transactionName string to be formatted
     * @param regexPatterns regular expression detailing the desired format
     * @return formatted string
     */
    @NotNull
    private Optional<String> getMatch(String transactionName, List<String> regexPatterns) {
        return regexPatterns.stream()
            .map(Pattern::compile)
            .map(pattern -> pattern.matcher(transactionName))
            .filter(Matcher::find)
            .findFirst().map(Matcher::group);
    }

    /**
     * Gets the description parser from plaid configuration properties and sets the transaction configuration properties
     * parser to this with it being specific for the institution the transactions belong to
     *
     * @param institutionId identifies the institution that the transaction belong to
     * @return the description parser with the correct configurations for the institution
     */
    @Nullable
    private PlaidConfigurationProperties.DescriptionParser getDescriptionParser(String institutionId) {
        PlaidConfigurationProperties.DescriptionParser descriptionParser = null;
        PlaidConfigurationProperties.TransactionConfigurationProperties transactions = transactionConfigurationProperties.getTransactions();
        if (transactions.getDescriptionParserForInstitution() != null) {
            descriptionParser = transactions.getDescriptionParserForInstitution().get(institutionId);
        }
        return descriptionParser;
    }

    /**
     * Gets the type group from plaid transaction by mapping from payment channel to type group
     *
     * @param transaction
     * @return
     */
    private String getTransactionTypeGroup(TransactionsGetResponse.Transaction transaction) {
        String typeGroup = transactionTypeGroupMap.getOrDefault(transaction.getPaymentChannel().replace(" ", ""), transaction.getPaymentChannel());
        log.info("Mapped Type Group: {} to: {}", transaction.getPaymentChannel(), typeGroup);
        return typeGroup;
    }

    /**
     * Gets the type group to set the dbs transaction from the transaction code in plaid transaction
     *
     * @param transaction
     * @return
     */
    private String getTransactionType(TransactionsGetResponse.Transaction transaction) {
        String type = transactionTypeMap.getOrDefault(transaction.getTransactionCode(), transaction.getTransactionCode());
        if (type == null) {
            type = "Credit/Debit Card";
        }

        log.info("Mapped Type: {} to: {}", transaction.getTransactionCode(), type);

        return type;
    }

}
