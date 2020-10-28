package com.backbase.proto.plaid.model;

import com.backbase.proto.plaid.converter.StringListConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * This class stores Webhook in a a table.
 */
@Getter
@Setter
@Entity
@Table(name = "webhook")
public class Webhook {

    public enum WebhookType {
        TRANSACTIONS,
        ITEM
    }

    public enum WebhookCode {
        INITIAL_UPDATE,
        HISTORICAL_UPDATE,
        DEFAULT_UPDATE,
        TRANSACTIONS_REMOVED,
        WEBHOOK_UPDATE_ACKNOWLEDGED,
        ERROR,
        USER_PERMISSION_REVOKED,
        PENDING_EXPIRATION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "webhook_type")
    @Enumerated(EnumType.STRING)
    private WebhookType webhookType;

    @Column(name = "webhook_code")
    @Enumerated(EnumType.STRING)
    private WebhookCode webhookCode;

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "error")
    @Lob
    private String error;

    @Column(name = "new_transactions")
    private Integer newTransactions;

    @Column(name = "removed_transactions")
    @Lob
    @Convert(converter = StringListConverter.class)
    private List<String> removedTransactions;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed")
    private boolean completed;

    @Lob
    @Column(name = "dbs_error")
    private String dbsError;


}
