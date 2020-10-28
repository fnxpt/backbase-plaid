package com.backbase.proto.plaid.model;

import com.backbase.proto.plaid.converter.LocationConverter;
import com.backbase.proto.plaid.converter.PaymentMetaConverter;
import com.backbase.proto.plaid.converter.StringListConverter;
import lombok.Data;
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

    @SuppressWarnings("java:S115")
    public enum PaymentChannel {
        online,
        in_store,
        other
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "pending")
    private boolean pending;

    @Column(name = "payment_channel")
    private PaymentChannel paymentChannel;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "iso_currency_code")
    private String isoCurrencyCode;

    @Column(name = "category")
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

    @Column(name = "payment_mata")
    @Lob
    @Convert(converter = PaymentMetaConverter.class)
    private PaymentMeta paymentMeta;

    @Data
    public static class Location {
        private String address;
        private String city;
        private String region;
        private String postalCode;
        private String country;
        private Integer lat;
        private Integer lon;
        private String storeNumber;
    }

    @Data
    public static class PaymentMeta {
        private String referenceNumber;
        private String ppdId;
        private String payee;
        private String byOrderOf;
        private String payer;
        private String paymentMethod;
        private String paymentProcessor;
        private String reason;

    }

}
