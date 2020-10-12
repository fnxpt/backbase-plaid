package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.configuration.PlaidConfiguration;
import com.backbase.proto.plaid.configuration.PlaidConfigurationProperties;
import com.backbase.proto.plaid.model.PlaidLinkRequest;
import com.backbase.proto.plaid.service.PlaidLinkService;
import com.plaid.client.PlaidClient;
import java.util.Arrays;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = PlaidApplication.class
)
@Slf4j
public class PlaidLinkServiceIT {

    static {
        System.setProperty("SIG_SECRET_KEY", "***REMOVED***");
    }

    @Autowired
    private PlaidLinkService plaidLinkService;

    @Test
    public void testCreateToken() {
        System.out.println("Hello World");
    }

    @Test
    public void testGetAccounts() {
        plaidLinkService.requestPlaidAccounts("access-testing");
    }

    @Test
    public void testIngestAccounts() {
        plaidLinkService.ingestPlaidAccounts(
            "access-testing",
            "lesley.knope",
            "8a808094748c4ca701749668ea030012");
    }

}