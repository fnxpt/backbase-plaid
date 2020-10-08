package com.backbase.proto.plaid.configuration;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "backbase.plaid")
@Data
public class PlaidConfigurationProperties {

    private String plaidClientID;

    private String plaidSecret;

    private String plaidEnv;

    private String plaidProducts;

    private String plaidCountryCodes;

}
