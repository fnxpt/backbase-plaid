package com.backbase.proto.plaid.configuration;

import com.backbase.dbs.transaction.presentation.service.ApiClient;
import com.backbase.dbs.transaction.presentation.service.api.TransactionsApi;
import com.backbase.stream.configuration.TransactionServiceConfiguration;
import com.plaid.client.PlaidClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(PlaidConfigurationProperties.class)
@Import(TransactionServiceConfiguration.class)
public class PlaidConfiguration {


    @Bean
    public TransactionsApi transactionsApi(ApiClient transactionPresentationApiClient) {
        return new TransactionsApi(transactionPresentationApiClient);
    }

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
