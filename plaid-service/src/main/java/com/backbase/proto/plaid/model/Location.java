package com.backbase.proto.plaid.model;

import lombok.Data;

@Data
public class Location {

    private String address;
    private String city;
    private String region;
    private String postal_code;
    private String country;
    private Integer lat;
    private Integer lon;
    private String store_number;

}
