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
 * This class maps Transactions retrieved from the Plaid API endpoint to the Backbase DBS Transactions.
 */
@Slf4j
@Component
public class PlaidToDBSTransactionMapper {

    private final PlaidConfigurationProperties transactionConfigurationProperties;
    private final Map<String, String> transactionTypeGroupMap;
    private final Map<String, String> transactionTypeMap;

    /**
     * Sets the configuration properties used for the mapping.
     *
     * @param transactionConfigurationProperties contains methods for setting the Type Group and Type for DBS Transactions
     */
    public PlaidToDBSTransactionMapper(PlaidConfigurationProperties transactionConfigurationProperties) {
        this.transactionConfigurationProperties = transactionConfigurationProperties;
        this.transactionTypeGroupMap = transactionConfigurationProperties.getTransactions().getTransactionTypeGroupMap();
        this.transactionTypeMap = transactionConfigurationProperties.getTransactions().getTransactionTypeMap();
    }
    /**
     * This maps the individual fields of the Plaid transaction to the Backbase Transaction.
     *
     * @param transaction a Transaction from the list retrieved by Plaid
     * @return DBS Transaction for ingestion
     */
    public TransactionItemPost map(TransactionsGetResponse.Transaction transaction, String institutionId) {

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
            log.debug("Amount: {} is negative", amount);
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
        log.debug("Mapped Billing Status: {}", billingStatus);

        if (transaction.getAuthorizedDate() != null) {
            bbTransaction.setValueDate(LocalDate.parse(transaction.getAuthorizedDate()));
        }


        return bbTransaction;
    }

    /**
     * Maps the descriptions from the Plaid transaction response to Backbase DBS Transaction
     * it uses the description parser in Plaid configuration properties to do so.
     *
     * @param transaction the Transaction response sent from Plaid
     * @param bbTransaction the Transaction to be ingested by Backbase
     * @param institutionId the identifier for the institution that the Transaction belongs to
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

        log.debug("Mapped transaction name: {} to description: {}", transactionName, description);

        description = StringUtils.abbreviate(description, 140);
        bbTransaction.setDescription(description);
    }


    /**
     * Maps the location from the Plaid returned Transaction which stores it as an object to the Backbase Transaction which stores it as separate fields.
     *
     * @param transaction Transaction parsed from Plaids Clients side
     * @param bbTransaction Backbase Transaction to be ingested and displayed in the front end
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
     * Maps Counter Party attributes from Plaid where they are less grouped.
     *
     * @param transaction the Transaction response from Plaid
     * @param bbTransaction the Backbase transaction to be ingested
     * @param institutionId identifies the institution the Transaction belongs to
     */
    private void mapCounterParty(TransactionsGetResponse.Transaction transaction, TransactionItemPost bbTransaction, String institutionId) {
        String counterpartyName;
        TransactionsGetResponse.Transaction.PaymentMeta paymentMeta = transaction.getPaymentMeta();

        if (transaction.getMerchantName() != null) {
            counterpartyName = transaction.getMerchantName();
            log.debug("Mapped counter party name from merchant name: {}", counterpartyName);
        } else if (paymentMeta.getPayee() != null) {
            counterpartyName = paymentMeta.getPayee();
            log.debug("Mapped counter party name from payee name: {}", counterpartyName);
        } else {
            PlaidConfigurationProperties.DescriptionParser descriptionParser = getDescriptionParser(institutionId);
            if (descriptionParser != null) {
                counterpartyName = parse(transaction.getName(), descriptionParser.getCounterPartyName());
                log.debug("Mapped counter party name: {} from transaction name: {}", transaction.getName(), counterpartyName);
            } else {
                counterpartyName = transaction.getName();
            }
        }
        if (counterpartyName.length() > 128) {
            log.debug("Counter party name: {} cannot be longer than 128 characters", counterpartyName);
            counterpartyName = StringUtils.abbreviate(counterpartyName, 128);
        }
        bbTransaction.setCounterPartyName(counterpartyName);
    }

    /**
     * Maps the Counter Party account number (BBAN) if it is available from Plaid.
     *
     * @param transaction the Transaction response from Plaid
     * @param bbTransaction the Backbase Transaction to be ingested
     * @param institutionId identifies the institution the Transaction belongs to
     */
    private void mapCounterPartyBBAN(TransactionsGetResponse.Transaction transaction, TransactionItemPost bbTransaction, String institutionId) {
        PlaidConfigurationProperties.DescriptionParser descriptionParser = getDescriptionParser(institutionId);
        if (descriptionParser != null) {
            getMatch(transaction.getName(), descriptionParser.getCounterPartyBBAN())
                .ifPresent(counterPartyAccountNumber -> {
                    log.debug("Mapping counter account number: {}", counterPartyAccountNumber);
                    bbTransaction.setCounterPartyAccountNumber(StringUtils.abbreviate(counterPartyAccountNumber, 36));
                });
        }

    }

    /**
     * Maps billing status from Plaid Transaction to Backbase Transaction.
     *
     * @param transaction the Transaction response from Plaid
     * @return the billing status to be added to the Backbase Transaction
     */
    @NotNull
    private String mapBilling(TransactionsGetResponse.Transaction transaction) {
        String billingStatus;

        if (Boolean.TRUE.equals(transaction.getPending()))
            billingStatus = "PENDING";
        else
            billingStatus = "BILLED";
        return billingStatus;
    }

    /**
     * Formats a text description to be mapped to a Transaction.
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
     * Formats a string using regular expression.
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
     * Gets the description parser from Plaid Configuration Properties and sets the Transaction Configuration Properties
     * parser to this with it being specific for the institution the Transactions belong to.
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
     * Gets the type group from Plaid Transaction by mapping from Payment Channel to Type Group.
     *
     * @param transaction
     * @return
     */
    private String getTransactionTypeGroup(TransactionsGetResponse.Transaction transaction) {
        String typeGroup = transactionTypeGroupMap.getOrDefault(transaction.getPaymentChannel().replace(" ", ""), transaction.getPaymentChannel());
        log.debug("Mapped Type Group: {} to: {}", transaction.getPaymentChannel(), typeGroup);
        return typeGroup;
    }

    /**
     * Gets the Type Group to set the DBS transaction from the Transaction Code in Plaid Transaction.
     *
     * @param transaction
     * @return
     */
    private String getTransactionType(TransactionsGetResponse.Transaction transaction) {
        String type = transactionTypeMap.getOrDefault(transaction.getTransactionCode(), transaction.getTransactionCode());
        if (type == null) {
            type = "Credit/Debit Card";
        }

        log.debug("Mapped Type: {} to: {}", transaction.getTransactionCode(), type);

        return type;
    }

}
