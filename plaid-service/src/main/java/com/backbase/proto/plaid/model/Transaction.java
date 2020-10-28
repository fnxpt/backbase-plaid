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

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "pending")
    private boolean pending;

    @Column(name = "payment_channel")
    private String paymentChannel;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "iso_currency_code")
    private String isoCurrencyCode;

    @Column(name = "categories")
    @Convert(converter = StringListConverter.class)
    private List<String> category;

    @Column(name = "name")
    private String name;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "authorized_date")
    private LocalDate authorizedDate;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "location")
    @Lob
    @Convert(converter = LocationConverter.class)
    private Location location;

    @Column(name = "payment_meta")
    @Lob
    @Convert(converter = PaymentMetaConverter.class)
    private PaymentMeta paymentMeta;

}
