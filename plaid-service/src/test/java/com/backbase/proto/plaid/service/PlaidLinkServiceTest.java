package com.backbase.proto.plaid.service;

import com.backbase.proto.plaid.configuration.PlaidConfiguration;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.model.PlaidLinkRequest;
import com.backbase.proto.plaid.model.PlaidLinkResponse;
import com.plaid.client.PlaidClient;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.backbase.proto.plaid.configuration.PlaidConfigurationProperties.CountryCode.GB;
import static com.backbase.proto.plaid.configuration.PlaidConfigurationProperties.Product.TRANSACTIONS;

//   PlaidLinkService plaidLinkService = new PlaidLinkService(plaidClient, plaidConfigurationProperties);

public class PlaidLinkServiceTest {

    @Test
    public void testCreateToken() {

        PlaidConfigurationProperties plaidConfigurationProperties = new PlaidConfigurationProperties();
        plaidConfigurationProperties.setClientId("5f55e12005b462001336b4d2");
        plaidConfigurationProperties.setSecret("0c13f91f9b77f4fcb584640f8383f8");
        plaidConfigurationProperties.setEnv(PlaidConfigurationProperties.Environment.SANDBOX);
        plaidConfigurationProperties.setProducts(Arrays.asList(TRANSACTIONS));
        plaidConfigurationProperties.setCountryCodes(Arrays.asList(GB));
        PlaidConfiguration plaidConfiguration = new PlaidConfiguration();

        PlaidClient plaidClient = plaidConfiguration.plaidClient(plaidConfigurationProperties);

        PlaidLinkService plaidLinkService = new PlaidLinkService(plaidClient, plaidConfigurationProperties, null,null,null,null,null);

       //plaidLinkService.createPlaidLink(new PlaidLinkRequest().language("en"));
    }

}