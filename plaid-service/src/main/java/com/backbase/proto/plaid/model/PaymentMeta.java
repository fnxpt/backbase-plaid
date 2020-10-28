package com.backbase.proto.plaid.model;

import lombok.Data;

@Data
public class PaymentMeta {
    private String referenceNumber;
    private String ppdId;
    private String payee;
    private String byOrderOf;
    private String payer;
    private String paymentMethod;
    private String paymentProcessor;
    private String reason;

}
