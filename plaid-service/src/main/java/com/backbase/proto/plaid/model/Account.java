package com.backbase.proto.plaid.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * This class is a Backbase DBS account table object, stores all the fields to be stored in the database labeled account.
 */
@Getter
@Setter
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "account_id")
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
