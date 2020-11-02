package com.backbase.proto.plaid.model;

import com.backbase.proto.plaid.converter.LocationConverter;
import com.backbase.proto.plaid.converter.PaymentMetaConverter;
import com.backbase.proto.plaid.converter.StringListConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * This class stores Transaction data in a table.
 */
@Getter
@Setter
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "iso_currency_code")
    private String isoCurrencyCode;

    @Column(name = "unofficial_currency_code")
    private String unofficialCurrencyCode;

    @Column(name = "categories")
    @Convert(converter = StringListConverter.class)
    private List<String> category;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "location")
    @Lob
    @Convert(converter = LocationConverter.class)
    private Location location;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "name")
    private String name;

    @Column(name = "original_description")
    private String originalDescription;

    @Column(name = "payment_meta")
    @Lob
    @Convert(converter = PaymentMetaConverter.class)
    private PaymentMeta paymentMeta;

    @Column(name = "pending")
    private boolean pending;

    @Column(name = "pending_transaction_id")
    private String pendingTransactionId;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "account_owner")
    private String accountOwner;

    @Column(name = "authorized_date")
    private LocalDate authorizedDate;


    @Column(name= "transaction_code")
    private String transactionCode;

    @Column(name = "payment_channel")
    private String paymentChannel;

//   Additional fields

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "ingested")
    private boolean ingested;

}
