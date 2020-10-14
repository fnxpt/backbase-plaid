package com.backbase.proto.plaid.mapper;

import com.backbase.dbs.transaction.presentation.service.model.CreditDebitIndicator;
import com.backbase.dbs.transaction.presentation.service.model.Currency;
import com.backbase.dbs.transaction.presentation.service.model.TransactionItemPost;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.plaid.client.response.TransactionsGetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

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


    public TransactionItemPost map(TransactionsGetResponse.Transaction transaction) {
        // CreditDebitIndicator credit = new CreditDebitIndicator();

        String arrangementId = transaction.getAccountId();

        Currency transactionAmountCurrency = new Currency();
        transactionAmountCurrency.setAmount(Double.toString(transaction.getAmount()));
        transactionAmountCurrency.setCurrencyCode(transaction.getIsoCurrencyCode());

        TransactionItemPost bbTransaction = new TransactionItemPost();
        bbTransaction.setExternalArrangementId(arrangementId);
        bbTransaction.setExternalId(transaction.getTransactionId());

        bbTransaction.setBookingDate(LocalDate.parse(transaction.getDate()));
        bbTransaction.setCreditDebitIndicator(CreditDebitIndicator.CRDT);
        bbTransaction.setTransactionAmountCurrency(transactionAmountCurrency);


        String description = (transaction.getPaymentMeta().getReason() == null) ? "" : transaction.getPaymentMeta().getReason();
        BigDecimal amount = BigDecimal.valueOf(transaction.getAmount());
        bbTransaction.setDescription(description);


        CreditDebitIndicator indicator;
        boolean amountIsNegative = amount.compareTo(BigDecimal.ZERO) < 0;
        if (amountIsNegative) {
            log.info("Amount: {} is negative", amount);
            indicator = CreditDebitIndicator.DBIT;
            amount = amount.negate();
        } else {
            indicator = CreditDebitIndicator.CRDT;
        }

        bbTransaction.setCreditDebitIndicator(indicator);


        String transactionTypeGroup = getTransactionTypeGroup(transaction);
        String transactionType = getTransactionType(transaction);

        bbTransaction.setTypeGroup(transactionTypeGroup);
        bbTransaction.setType(transactionType);


        return bbTransaction;

    }


    private String getTransactionTypeGroup(TransactionsGetResponse.Transaction transaction) {
        String typeGroup = transactionTypeGroupMap.getOrDefault(transaction.getPaymentChannel().replace(" ", ""), transaction.getPaymentChannel());
        log.info("Mapped Type Group: {} to: {}", transaction.getPaymentChannel(), typeGroup);
        return typeGroup;
    }

    private String getTransactionType(TransactionsGetResponse.Transaction transaction) {
        String type = transactionTypeMap.getOrDefault(transaction.getTransactionCode(), transaction.getTransactionCode());
        if(type == null) {
            type = "Credit/Debit Card";
        }

        log.info("Mapped Type: {} to: {}", transaction.getTransactionCode(), type);

        return type;
    }

    private PlaidConfigurationProperties.TransactionConfigurationProperties getTransactionsMapping() {
        return transactionConfigurationProperties.getTransactions();
    }

}
