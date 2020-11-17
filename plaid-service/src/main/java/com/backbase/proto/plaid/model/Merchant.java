package com.backbase.proto.plaid.model;

import com.backbase.proto.plaid.converter.LocationConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "location")
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @Column(name = "name")
    private String name;

    @Column(name = "logo")
    private String logo;

    @Column(name = "website")
    private String website;

    @Column(name = "location")
    @Convert(converter = LocationConverter.class)
    private Location location;




}
