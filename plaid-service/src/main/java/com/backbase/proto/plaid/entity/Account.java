package com.backbase.proto.plaid.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Getter
@Setter
@Entity
@Table(name = "account")
public class Account {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_id_generator")
    @GenericGenerator(
        name = "account_id_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = @Parameter(name = "sequence_name", value = "account_seq")
    )
    private Long id;

    @Column(name ="item_id")
    private String itemId;

    @Column(name ="account_id")
    private String accountId;

    @Column(name = "mask")
    private String mask;

    @Column(name = "name")
    private String name;

    @Column(name = "sub_type")
    private String subtype;

    @Column(name = "type")
    private String type;



}
