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

@Slf4j
@Component
public class TransactionMapper {

    private final PlaidConfigurationProperties transactionConfigurationProperties;
    private final Map<String, String> transactionTypeGroupMap;
    private final Map<String, String> transactionTypeMap;

    public TransactionMapper(PlaidConfigurationProperties transactionConfigurationProperties) {
        this.transactionConfigurationProperties = transactionConfigurationProperties;
        this.transactionTypeGroupMap = transactionConfigurationProperties.getTransactions().getTransactionTypeGroupMap();
        this.transactionTypeMap = transactionConfigurationProperties.getTransactions().getTransactionTypeMap();
    }

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



    private void mapLocation(TransactionsGetResponse.Transaction transaction, TransactionItemPost bbTransaction) {
        TransactionsGetResponse.Transaction.Location location = transaction.getLocation();

        if (location != null) {
            bbTransaction.setCounterPartyCity(location.getCity());
            bbTransaction.setCounterPartyAddress(location.getAddress());
            bbTransaction.setCounterPartyCountry(location.getCountry());
        }
    }

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


    @NotNull
    private String mapBilling(TransactionsGetResponse.Transaction transaction) {
        String billingStatus;

        if (transaction.getPending())
            billingStatus = "PENDING";
        else
            billingStatus = "BILLED";
        return billingStatus;
    }

    private String parse(String text, List<String> regexPatterns) {
        String description;
        description = getMatch(text, regexPatterns)
            .orElse(text);
        return description;
    }

    @NotNull
    private Optional<String> getMatch(String transactionName, List<String> regexPatterns) {
        return regexPatterns.stream()
            .map(Pattern::compile)
            .map(pattern -> pattern.matcher(transactionName))
            .filter(Matcher::find)
            .findFirst().map(Matcher::group);
    }

    @Nullable
    private PlaidConfigurationProperties.DescriptionParser getDescriptionParser(String institutionId) {
        PlaidConfigurationProperties.DescriptionParser descriptionParser = null;
        PlaidConfigurationProperties.TransactionConfigurationProperties transactions = transactionConfigurationProperties.getTransactions();
        if (transactions.getDescriptionParserForInstitution() != null) {
            descriptionParser = transactions.getDescriptionParserForInstitution().get(institutionId);
        }
        return descriptionParser;
    }


    private String getTransactionTypeGroup(TransactionsGetResponse.Transaction transaction) {
        String typeGroup = transactionTypeGroupMap.getOrDefault(transaction.getPaymentChannel().replace(" ", ""), transaction.getPaymentChannel());
        log.info("Mapped Type Group: {} to: {}", transaction.getPaymentChannel(), typeGroup);
        return typeGroup;
    }

    private String getTransactionType(TransactionsGetResponse.Transaction transaction) {
        String type = transactionTypeMap.getOrDefault(transaction.getTransactionCode(), transaction.getTransactionCode());
        if (type == null) {
            type = "Credit/Debit Card";
        }

        log.info("Mapped Type: {} to: {}", transaction.getTransactionCode(), type);

        return type;
    }

}
