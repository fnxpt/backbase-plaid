package com.backbase.proto.plaid.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Getter
@Setter
@Entity
@Table(name = "item")
public class Item {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_id_generator")
    @GenericGenerator(
        name = "item_id_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = @Parameter(name = "sequence_name", value = "item_seq")
    )
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

}
