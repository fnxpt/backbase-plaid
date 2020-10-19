package com.backbase.proto.plaid.configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Registers the data to be used to set, build the plaid
 */

@ConfigurationProperties(prefix = "backbase.plaid")
@Data
public class PlaidConfigurationProperties {
    /**
     * Name of the client
     */
    @NotNull
    private String clientName;
    /**
     * client identification
     */
    @NotNull
    private String clientId;
    /**
     * secret used to initialise the link
     */
    @NotNull
    private String secret;
    /**
     * notifies when data is available for retrieval
     */
    @NotNull
    private String webhookBaseUrl;
    /**
     * Indicates the environment to be built
     */
    @NotNull
    private Environment env;
    /**
     * Stores the Products that are to be used
     */
    @NotNull
    private List<Product> products;
    /**
     * Stores the country codes that are to be available
     */
    @NotNull
    private List<CountryCode> countryCodes;
    /**
     *
     */
    @NotNull
    private List<String> defaultReferenceJobRoleNames;



    /**
     * environments for the plaid client
     */
    public enum Environment {
        SANDBOX, DEVELOPMENT, PRODUCTION
    }

    /**
     * available Product types
     */
    public enum Product {
        TRANSACTIONS, AUTH, IDENTITY, ASSETS, INVESTMENTS, LIABILITIES, PAYMENT_INITIATION
    }

    /**
     * Types of country code available
     */
    public enum CountryCode {
        US, CA, ES, FR, GB, IE, NL
    }

    private TransactionConfigurationProperties transactions;

    /**
     * Transaction specific Properties, Type group and Type both of which must be mapped from Plaid
     * to backbase DBS
     */
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

        private Map<String, DescriptionParser> descriptionParserForInstitution;

    }

    /**
     *
     */
    @Data
    public static class DescriptionParser {

        private List<String> counterPartyName;
        private List<String> counterPartyBBAN;
        private List<String> description;
        private List<String> cardId;
        private List<String> valueDate;
        private List<String> bookingDate;
    }

}
