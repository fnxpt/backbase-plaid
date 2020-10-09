package com.backbase.proto.plaid.configuration;

import com.plaid.client.PlaidClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PlaidConfigurationProperties.class)
public class PlaidConfiguration {

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
