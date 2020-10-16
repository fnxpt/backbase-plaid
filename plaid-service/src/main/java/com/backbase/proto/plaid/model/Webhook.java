package com.backbase.proto.plaid.model;

import com.backbase.proto.plaid.converter.StringListConverter;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

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
        ERROR
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
