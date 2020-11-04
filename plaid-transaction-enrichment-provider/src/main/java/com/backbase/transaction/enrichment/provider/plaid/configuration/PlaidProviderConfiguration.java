package com.backbase.transaction.enrichment.provider.plaid.configuration;

import com.backbase.proto.plaid.service.ApiClient;
import com.backbase.proto.plaid.service.api.CategoriesApi;
import com.backbase.proto.plaid.service.api.EnrichApi;
import com.backbase.transaction.enrichment.provider.plaid.api.PlaidCategoryLoader;
import com.backbase.transaction.enrichment.provider.plaid.api.PlaidTransactionEnrichmentLoader;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "backbase.plaid")
@Setter
@Slf4j
@Validated
public class PlaidProviderConfiguration {
    /**
     * PlaidServiceBaseUrl
     */
    @NotNull
    private String serviceBaseUrl;


    @Bean(name ="plaidApiClient")
    public ApiClient plaidApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(serviceBaseUrl);
        return apiClient;
    }

    @Bean(name = "plaidEnrichApi")
    public EnrichApi enrichApi(@Qualifier("plaidApiClient") ApiClient apiClient) {
        return new EnrichApi(apiClient);
    }

    @Bean(name = "plaidCategoriesApi")
    public CategoriesApi categoriesApi(@Qualifier("plaidApiClient") ApiClient apiClient) {
        return new CategoriesApi(apiClient);
    }

    @Bean(name = "plaidTransactionEnrichmentLoader")
    public PlaidTransactionEnrichmentLoader plaidTransactionEnrichmentLoader(@Qualifier("plaidEnrichApi") EnrichApi enrichApi) {
        return new PlaidTransactionEnrichmentLoader(enrichApi);
    }

    @Bean(name = "plaidCategoryLoader")
    public PlaidCategoryLoader plaidCategoryLoader(@Qualifier("plaidCategoriesApi") CategoriesApi categoriesApi) {
        return new PlaidCategoryLoader(categoriesApi);
    }


}
