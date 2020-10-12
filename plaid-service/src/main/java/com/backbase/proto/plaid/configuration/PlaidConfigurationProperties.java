package com.backbase.proto.plaid.configuration;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "backbase.plaid")
@Data
public class PlaidConfigurationProperties {

    @NotNull
    private String clientName;

    @NotNull
    private String clientId;

    @NotNull
    private String secret;

    @NotNull
    private Environment env;

    @NotNull
    private List<Product> products;

    @NotNull
    private List<CountryCode> countryCodes;

    @NotNull
    private List<String> defaultReferenceJobRoleNames;


    public enum Environment {
        SANDBOX, DEVELOPMENT, PRODUCTION
    }

    public enum Product {
        TRANSACTIONS, AUTH, IDENTITY, ASSETS, INVESTMENTS, LIABILITIES, PAYMENT_INITIATION
    }

    public enum CountryCode {
        US, CA, ES, FR, GB, IE, NL
    }

}
