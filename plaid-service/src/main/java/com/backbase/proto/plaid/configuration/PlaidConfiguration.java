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
            .clientIdAndSecret(configuration.getPlaidClientID(), configuration.getPlaidSecret());

        switch (configuration.getPlaidEnv()) {
            case "sandbox":
                builder = builder.sandboxBaseUrl();
                break;
            case "development":
                builder = builder.developmentBaseUrl();
                break;
            case "production":
                builder = builder.productionBaseUrl();
                break;
            default:
                throw new IllegalArgumentException("unknown environment: " + configuration.getPlaidEnv());
        }

        return builder.build();

    }


}
