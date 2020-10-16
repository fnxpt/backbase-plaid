package com.backbase.proto.plaid.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Data;
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
    private String webhookBaseUrl;

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

    private TransactionConfigurationProperties transactions;

    @Data
    public static class TransactionConfigurationProperties {

        /**
         * Mapping between Mambu Transaction Type Groups and Backbase DBS Transaction Type Groups.
         */
        private Map<String, String> transactionTypeGroupMap = new HashMap<>();

        /**
         * Mapping between Mambu Transaction Type and Backbase DBS Transaction Type.
         */
        private Map<String, String> transactionTypeMap = new HashMap<>();

    }

}
