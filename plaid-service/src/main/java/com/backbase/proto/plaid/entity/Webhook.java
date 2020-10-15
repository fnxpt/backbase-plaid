package com.backbase.proto.plaid.entity;

import java.util.List;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Webhook {

    @Id
    private Long id;

    public enum WebhookType {
        TRANSACTIONS,
        ITEM
    }

    public enum WebhookCode{
        INITIAL_UPDATE,
        HISTORICAL_UPDATE,
        DEFAULT_UPDATE,
        TRANSACTIONS_REMOVED,
        WEBHOOK_UPDATE_ACKNOWLEDGED,
        ERROR
    }

    @Enumerated(EnumType.STRING)
    private WebhookType webhookType;

    @Enumerated(EnumType.STRING)
    private WebhookCode webhookCode;

    private String itemId;

    private String error;

    private Integer newTransactions;

    private List<String> removedTransactions = null;


}
