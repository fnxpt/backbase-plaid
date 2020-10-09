package com.backbase.proto.plaid.configuration;

import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "backbase.plaid")
@Data
public class PlaidConfigurationProperties {

    @NotNull
    private String clientId;

    @NotNull
    private String secret;

    @NotNull
    private Environment env;

    @NotNull
    private Product[] products;

    @NotNull
    private String[] countryCodes;


    public enum Environment {
        SANDBOX, DEVELOPMENT, PRODUCTION
    }

    public enum Product {
        TRANSACTIONS, AUTH, IDENTITY, ASSETS, INVESTMENTS, LIABILITIES, PAYMENT_INITIATION
    }

}
