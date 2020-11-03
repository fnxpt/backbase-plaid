package com.backbase.proto.plaid.provider.configuration;

import com.backbase.proto.plaid.provider.CategoryService;
import com.backbase.proto.plaid.provider.PlaidCategoryLoader;
import com.backbase.proto.plaid.provider.PlaidTransactionEnrichmentLoader;
import com.plaid.client.PlaidClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PlaidConfigurationProperties.class)
@Slf4j
public class PlaidTransactionEnricherConfiguration {

    public PlaidTransactionEnricherConfiguration() {
        log.info("Loading Plaid Transaction Configuration");
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

    @Bean
    public CategoryService categoryService() {
        return new CategoryService();
    }

    @Bean
    public PlaidCategoryLoader plaidCategoryLoader(CategoryService categoryService, PlaidClient plaidClient) {
        return new PlaidCategoryLoader(categoryService, plaidClient);
    }

    @Bean
    public PlaidTransactionEnrichmentLoader plaidTransactionEnrichmentLoader(PlaidCategoryLoader plaidCategoryLoader, PlaidClient plaidClient) {
        return new PlaidTransactionEnrichmentLoader(plaidCategoryLoader, plaidClient);
    }


}
