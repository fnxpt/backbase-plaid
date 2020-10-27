package com.backbase.proto.plaid.configuration;

import com.backbase.dbs.transaction.presentation.service.ApiClient;
import com.backbase.dbs.transaction.presentation.service.api.TransactionsApi;
import com.backbase.stream.configuration.TransactionServiceConfiguration;
import com.plaid.client.PlaidClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * This class is used to build and set properties of Plaid Client.
 */
@Configuration
@EnableConfigurationProperties(PlaidConfigurationProperties.class)
@Import(TransactionServiceConfiguration.class)
public class PlaidConfiguration {


    /**
     * Instantiates the Transaction API with a api parsed as an argument.
     * It allows access to the Transactions through authentication and for responses and request for Transactions to be managed easily.
     *
     * @param transactionPresentationApiClient api to set the Transactions API
     * @return Transactions API
     */
    @Bean
    public TransactionsApi transactionsApi(ApiClient transactionPresentationApiClient) {
        return new TransactionsApi(transactionPresentationApiClient);
    }

    /**
     * Builds and returns a Plaid Client which is used to sent requests to Plaid.
     * It builds the Client using the Configuration Properties (Configuration).
     * Builds an environment for the client, the type of which is specified in the configuration perimeter.
     *
     * @param configuration: contains properties for configuring the Plaid Client and required to build it
     * @return the Plaid Client built with configurations and an environment
     */
    @Bean
    public PlaidClient plaidClient(PlaidConfigurationProperties configuration) {

        PlaidClient.Builder builder = PlaidClient.newBuilder()
            .clientIdAndSecret(configuration.getClientId(), configuration.getSecret());

        switch (configuration.getEnv()) {
            case SANDBOX:
                builder = builder.sandboxBaseUrl();
                break;
            case DEVELOPMENT:
                builder = builder.developmentBaseUrl();
                break;
            case PRODUCTION:
                builder = builder.productionBaseUrl();
                break;
            default:
                throw new IllegalArgumentException("unknown environment: " + configuration.getEnv());
        }

        return builder.build();

    }


}
