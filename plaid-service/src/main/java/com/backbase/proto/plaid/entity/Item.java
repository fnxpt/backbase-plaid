package com.backbase.proto.plaid.entity;

import com.backbase.proto.plaid.model.PlaidAccount;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.annotation.Id;

@Table
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_id_generator")
    @GenericGenerator(
        name = "item_id_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = @Parameter(name = "sequence_name", value = "plaid_item_seq")
    )
    private Long id;

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "access_tokne")
    private String accessToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;


    private List<Account> accounts;

}
