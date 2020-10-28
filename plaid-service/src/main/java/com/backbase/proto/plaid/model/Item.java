package com.backbase.proto.plaid.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * This class stores Item data in a table.
 */
@Getter
@Setter
@Entity
@Table(name = "item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "institution_id")
    private String institutionId;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "state")
    private String state;

    @Column(name = "state_changed_date")
    private LocalDateTime stateChangedDate;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "error_display_message")
    private String errorDisplayMessage;

}
