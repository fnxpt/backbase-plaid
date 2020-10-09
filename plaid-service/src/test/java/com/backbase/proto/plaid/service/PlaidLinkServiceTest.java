package com.backbase.proto.plaid.service;

import com.backbase.proto.plaid.configuration.PlaidConfiguration;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.model.PlaidLinkRequest;
import com.plaid.client.PlaidClient;
import org.junit.Test;


public class PlaidLinkServiceTest {

    @Test
    public void testCreateToken() {

        PlaidConfigurationProperties plaidConfigurationProperties = new PlaidConfigurationProperties();
        plaidConfigurationProperties.setClientId("fskdlfdl");

        PlaidConfiguration plaidConfiguration = new PlaidConfiguration();

        PlaidClient plaidClient = plaidConfiguration.plaidClient(plaidConfigurationProperties);


        PlaidLinkService plaidLinkService = new PlaidLinkService(plaidClient, plaidConfigurationProperties, null);

        plaidLinkService.createPlaidLink(new PlaidLinkRequest());


    }

}