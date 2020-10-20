package com.backbase.proto.plaid.model;

import com.backbase.proto.plaid.converter.StringListConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * This class stores Institution fields in a table.
 */
@Getter
@Setter
@Entity
@Table(name = "institution")
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "institution_id")
    private String institutionId;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @Column(name = "logo")
    private String logo;

    @Column(name = "primaryColor")
    private String primaryColor;

    @Column(name = "first_created_at")
    private LocalDateTime firstRegisteredAt;

    @Column(name = "first_created_by")
    private String firstCreatedBy;

    @Column(name = "routing_numbers")
    @Lob
    @Convert(converter = StringListConverter.class)
    private List<String> routingNumbers;


}
