package com.backbase.proto.plaid.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Account {

    @Id
    private Long id;

    private String accountId;

    private String mask;

    private String name;

    private String subtype;

    private String type;



}
